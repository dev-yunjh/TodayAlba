package com.example.albaz;

import com.example.albaz.global.security.JwtProvider;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.BadJwtException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class JwtProviderTests {
    @Autowired
    private JwtProvider provider;

    @Test
    void tokensIdentifyTheirUserAndCanOnlyBeUsedForTheirIntendedPurpose() {
        UUID userId = UUID.randomUUID();
        String access = provider.createAccessToken(userId);
        String refresh = provider.createRefreshToken(userId, false);

        assertEquals(userId, provider.verifyAccessToken(access));
        assertEquals(userId, provider.verifyRefreshToken(refresh));
        assertThrows(BadJwtException.class, () -> provider.verifyAccessToken(refresh));
        assertThrows(BadJwtException.class, () -> provider.verifyRefreshToken(access));
    }
}
