package com.example.todayalba;

import com.example.todayalba.user.dto.UserCreateRequest;
import com.example.todayalba.user.domain.EmailVerification;
import com.example.todayalba.user.exception.VerificationCodeException;
import com.example.todayalba.user.exception.VerificationCooldownException;
import com.example.todayalba.user.mail.VerificationMailSender;
import com.example.todayalba.user.repository.UserRepository;
import com.example.todayalba.user.service.EmailVerificationService;
import com.example.todayalba.user.service.UserService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class EmailSignupTests {
    @Autowired EmailVerificationService verifications;
    @Autowired UserService users;
    @Autowired UserRepository repository;
    @Autowired WebApplicationContext context;
    @MockitoBean VerificationMailSender sender;

    @Test
    void verifiedCodeRegistersOnlyOnce() {
        String email = "signup@example.com";
        verifications.requestCode("Signup@Example.com");
        var sent = ArgumentCaptor.forClass(String.class);
        verify(sender).sendCode(eq(email), sent.capture());
        assertThrows(VerificationCooldownException.class, () -> verifications.requestCode(email));
        String token = verifications.verifyCode(email, sent.getValue());
        assertEquals("VERIFICATION_CODE_UNAVAILABLE", assertThrows(VerificationCodeException.class,
                () -> verifications.verifyCode(email, sent.getValue())).getCode());
        var request = new UserCreateRequest(token, "password", "tester");
        users.register(request);
        assertEquals(1L, repository.count());
        assertThrows(ResponseStatusException.class, () -> users.register(request));
    }

    @Test
    void failedAttemptsPersistAcrossRequests() throws Exception {
        String email = "attempts@example.com";
        verifications.requestCode(email);
        var sent = ArgumentCaptor.forClass(String.class);
        verify(sender).sendCode(eq(email), sent.capture());
        String wrong = sent.getValue().equals("000000") ? "000001" : "000000";
        var mvc = MockMvcBuilders.webAppContextSetup(context).build();
        mvc.perform(post("/api/email-verifications/confirm").contentType("application/json")
                        .content("""
                                {"email":"%s","code":"%s"}
                                """.formatted(email, wrong)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VERIFICATION_CODE_MISMATCH"))
                .andExpect(jsonPath("$.message").value("인증번호가 일치하지 않습니다."));
        for (int attempt = 2; attempt <= 4; attempt++) {
            assertEquals("VERIFICATION_CODE_MISMATCH", assertThrows(VerificationCodeException.class,
                    () -> verifications.verifyCode(email, wrong)).getCode());
        }
        assertEquals("VERIFICATION_ATTEMPTS_EXCEEDED", assertThrows(VerificationCodeException.class,
                () -> verifications.verifyCode(email, wrong)).getCode());
        assertEquals("VERIFICATION_ATTEMPTS_EXCEEDED", assertThrows(VerificationCodeException.class,
                () -> verifications.verifyCode(email, sent.getValue())).getCode());
    }

    @Test
    void expiredCodeHasItsOwnFailureReason() {
        var verification = new EmailVerification("email-hash");
        Instant issuedAt = Instant.EPOCH;
        verification.issueCode("code-hash", issuedAt);
        assertDoesNotThrow(() -> verification.ensureCodeCanBeChecked(issuedAt.plusSeconds(149)));
        var error = assertThrows(VerificationCodeException.class,
                () -> verification.ensureCodeCanBeChecked(issuedAt.plusSeconds(150)));
        assertEquals("VERIFICATION_CODE_EXPIRED", error.getCode());
        assertEquals("인증번호가 만료되었습니다. 새 인증번호를 요청해 주세요.", error.getMessage());
    }

    @Test
    void signupTokenExpiresFiveMinutesAfterConfirmation() {
        var verification = new EmailVerification("email-hash");
        Instant confirmedAt = Instant.EPOCH;
        verification.confirm("token-hash", confirmedAt);
        assertTrue(verification.tokenValidAt(confirmedAt.plusSeconds(299)));
        assertFalse(verification.tokenValidAt(confirmedAt.plusSeconds(300)));
    }
}
