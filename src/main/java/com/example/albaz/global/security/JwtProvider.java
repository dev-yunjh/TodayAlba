package com.example.albaz.global.security;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
    private final NimbusJwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    private final String issuer;
    private final String audience;
    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final Duration rememberMeRefreshTtl;

    // Spring이 이 빈을 만들 때 @Value로 서명 키, 발급자, 대상 API, 토큰별 유효 기간(TTL)을 주입한다.
    public JwtProvider(@Value("${app.auth.jwt-secret}") String encodedSecret,
                       @Value("${app.auth.issuer}") String issuer,
                       @Value("${app.auth.audience}") String audience,
                       @Value("${app.auth.access-token-ttl}") Duration accessTtl,
                       @Value("${app.auth.refresh-token-ttl}") Duration refreshTtl,
                       @Value("${app.auth.remember-me-refresh-token-ttl}") Duration rememberMeRefreshTtl) {
        // 설정에 Base64 문자열로 저장한 키를 원래 바이트로 복원한다. HS256에는 최소 32바이트가 필요하다.
        byte[] secret = Base64.getDecoder().decode(encodedSecret);
        if (secret.length < 32) {
            throw new IllegalArgumentException("JWT signing key must contain at least 32 decoded bytes");
        }
        // HS256은 같은 비밀 키로 발급할 때 서명을 만들고, 검증할 때 그 서명이 맞는지 확인한다.
        SecretKeySpec key = new SecretKeySpec(secret, "HmacSHA256");
        this.encoder = NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build();
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        // 만료 시각(exp)을 필수로 하고, 서버 간 시계 차이에 따른 유예 시간은 0으로 설정한다.
        JwtTimestampValidator timestamps = new JwtTimestampValidator(Duration.ZERO);
        timestamps.setAllowEmptyExpiryClaim(false);
        // 이후 decode()를 호출할 때마다 발급자(iss)와 대상 API(aud)도 설정값과 대조한다.
        decoder.setJwtValidator(JwtValidators.createDefaultWithValidators(List.of(
                timestamps, new JwtIssuerValidator(issuer), new JwtAudienceValidator(audience))));
        this.issuer = issuer;
        this.audience = audience;
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
        this.rememberMeRefreshTtl = rememberMeRefreshTtl;
    }

    // 로그인 성공 후 일반 API 요청에 사용할 액세스 토큰을 발급한다.
    public String createAccessToken(UUID userId) {
        return issue(userId, "access", accessTtl).getTokenValue();
    }

    // 재발급에 쓸 리프레시 토큰을 만든다. '로그인 유지'를 선택하면 더 긴 유효 기간을 쓴다.
    public String createRefreshToken(UUID userId, boolean rememberMe) {
        return issueRefreshToken(userId, rememberMe).getTokenValue();
    }

    public Jwt issueRefreshToken(UUID userId, boolean rememberMe) {
        return issue(userId, "refresh", rememberMe ? rememberMeRefreshTtl : refreshTtl);
    }

    private Jwt issue(UUID userId, String tokenUse, Duration ttl) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        // 두 종류의 토큰이 공통으로 담는 정보(claim)를 구성한다. ttl은 지금부터 만료까지의 기간이다.
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .audience(List.of(audience))
                // sub는 사용자 ID다. 같은 사용자가 토큰을 여러 번 발급받아도 이 값은 같다.
                .subject(userId.toString())
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                // jti는 이번에 발급하는 토큰 자체의 ID다. 발급할 때마다 새로 만든다.
                .id(UUID.randomUUID().toString())
                .claim("token_use", tokenUse)
                .build();
        return encoder.encode(JwtEncoderParameters.from(claims));
    }

    public UUID verifyAccessToken(String token) {
        return verify(token, "access");
    }

    public UUID verifyRefreshToken(String token) {
        return verify(token, "refresh");
    }

    private UUID verify(String token, String expectedUse) {
        // 서명과 생성자에서 등록한 검증 규칙을 통과해야 다음 줄에서 토큰 내용을 사용할 수 있다.
        Jwt jwt = decoder.decode(token);
        // 호출한 메서드가 기대하는 용도(access 또는 refresh)와 다르거나 사용자 ID가 없으면 거부한다.
        if (!expectedUse.equals(jwt.getClaimAsString("token_use")) || jwt.getSubject() == null) {
            throw new BadJwtException("Invalid token use or missing subject");
        }
        try {
            // 발급할 때 sub에 문자열로 담았던 사용자 ID를 도메인에서 쓰는 UUID로 돌려준다.
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException ex) {
            throw new BadJwtException("Invalid user ID in token", ex);
        }
    }
}
