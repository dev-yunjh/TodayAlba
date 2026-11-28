package com.example.albaz;

import com.example.albaz.global.security.JwtProvider;
import com.example.albaz.user.dto.LoginRequest;
import com.example.albaz.user.dto.UserCreateRequest;
import com.example.albaz.user.mail.VerificationMailSender;
import com.example.albaz.user.service.EmailVerificationService;
import com.example.albaz.user.service.UserService;
import jakarta.servlet.Filter;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class LoginApiTests {
    @Autowired WebApplicationContext context;
    @Autowired @Qualifier("springSecurityFilterChain") Filter security;
    @Autowired EmailVerificationService verifications;
    @Autowired UserService users;
    @Autowired JwtProvider jwt;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate db;
    @MockitoBean VerificationMailSender sender;
    private MockMvc mvc;

    @BeforeEach
    void registerUser() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(security).build();
        String email = "login@example.com";
        verifications.requestCode(email);
        var code = ArgumentCaptor.forClass(String.class);
        verify(sender).sendCode(eq(email), code.capture());
        users.register(new UserCreateRequest(verifications.verifyCode(email, code.getValue()), "TestPass1!", "loginUser"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void registeredUserGetsTokensForSelectedLoginDuration(boolean rememberMe) throws Exception {
        var request = json.createObjectNode().put("email", "Login@Example.com").put("password", "TestPass1!");
        if (rememberMe) {
            request.put("rememberMe", true);
        }
        var result = mvc.perform(post("/api/login").contentType("application/json")
                        .content(json.writeValueAsBytes(request)))
                .andExpect(status().isOk()).andReturn();
        var data = json.readTree(result.getResponse().getContentAsByteArray()).get("data");
        UUID id = jwt.verifyAccessToken(data.get("accessToken").asString());
        String refreshToken = data.get("refreshToken").asString();
        assertEquals(id, jwt.verifyRefreshToken(refreshToken));
        var claims = SignedJWT.parse(refreshToken).getJWTClaimsSet();
        assertEquals(Duration.ofDays(rememberMe ? 14 : 1),
                Duration.between(claims.getIssueTime().toInstant(), claims.getExpirationTime().toInstant()));
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(refreshToken.getBytes(StandardCharsets.UTF_8));
        assertEquals(LocalDateTime.ofInstant(claims.getExpirationTime().toInstant(), ZoneOffset.UTC),
                db.queryForObject("""
                        select s.expires_at from login_sessions s join users u on u.id = s.user_id
                        where u.nickname = 'loginUser' and s.refresh_token_hash = ?
                        """, LocalDateTime.class, hash));
        assertEquals(1, db.queryForObject("select count(*) from users where nickname = 'loginUser' and last_login_at is not null", Integer.class));
    }

    @Test
    void logoutRevokesOnlyThePresentedRefreshTokenAndCanBeRetried() throws Exception {
        var request = new LoginRequest("login@example.com", "TestPass1!", false);
        var first = users.login(request);
        var second = users.login(request);

        logout(first.accessToken());
        assertEquals(2, db.queryForObject("select count(*) from login_sessions", Integer.class));
        logout(first.refreshToken());
        logout(first.refreshToken());
        assertEquals(1, db.queryForObject("select count(*) from login_sessions", Integer.class));
        logout(second.refreshToken());
        assertEquals(0, db.queryForObject("select count(*) from login_sessions", Integer.class));
    }

    private void logout(String token) throws Exception {
        mvc.perform(post("/api/logout").contentType("application/json")
                        .content(json.writeValueAsBytes(json.createObjectNode().put("refreshToken", token))))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void rejectsWrongPasswordUnknownEmailAndSuspendedUser() throws Exception {
        assertLoginFailed("login@example.com", "WrongPass1!");
        assertLoginFailed("unknown@example.com", "TestPass1!");
        db.update("update users set status = 'SUSPENDED' where nickname = 'loginUser'");
        assertLoginFailed("login@example.com", "TestPass1!");
        assertEquals(0, db.queryForObject("select count(*) from login_sessions", Integer.class));
        assertEquals(0, db.queryForObject("select count(*) from users where nickname = 'loginUser' and last_login_at is not null", Integer.class));
    }

    private void assertLoginFailed(String email, String password) throws Exception {
        mvc.perform(post("/api/login").contentType("application/json").content("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
