package com.example.albaz;

import com.example.albaz.user.domain.EmailVerification;
import com.example.albaz.user.repository.EmailVerificationRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EmailVerificationCleanupTests {
    @Autowired EmailVerificationRepository verifications;

    @Test
    void deletesExpiredRecordsAndKeepsValidCodesAndTokens() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        var expiredCode = new EmailVerification("expired-code");
        expiredCode.issueCode("code", now.minusSeconds(150));

        var expiredToken = new EmailVerification("expired-token");
        expiredToken.issueCode("code", now.minusSeconds(320));
        expiredToken.confirm("token-a", now.minusSeconds(300));

        var activeToken = new EmailVerification("active-token");
        activeToken.issueCode("code", now.minusSeconds(320));
        activeToken.confirm("token-b", now.minusSeconds(299));

        var reissuedCode = new EmailVerification("reissued-code");
        reissuedCode.issueCode("old-code", now.minusSeconds(400));
        reissuedCode.confirm("token-c", now.minusSeconds(350));
        reissuedCode.issueCode("new-code", now.minusSeconds(149));

        verifications.saveAllAndFlush(List.of(expiredCode, expiredToken, activeToken, reissuedCode));
        assertEquals(2, verifications.deleteExpired(now));
        assertFalse(verifications.existsById("expired-code"));
        assertFalse(verifications.existsById("expired-token"));
        assertTrue(verifications.existsById("active-token"));
        assertTrue(verifications.existsById("reissued-code"));
    }
}
