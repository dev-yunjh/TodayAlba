package com.example.albaz.user.service;

import com.example.albaz.global.security.JwtProvider;
import com.example.albaz.user.domain.LoginSession;
import com.example.albaz.user.domain.Status;
import com.example.albaz.user.domain.User;
import com.example.albaz.user.dto.LoginRequest;
import com.example.albaz.user.dto.LoginResponse;
import com.example.albaz.user.dto.UserCreateRequest;
import com.example.albaz.user.exception.LoginFailedException;
import com.example.albaz.user.repository.LoginSessionRepository;
import com.example.albaz.user.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final LoginSessionRepository loginSessions;

    @Transactional
    public void register(@Valid @NotNull UserCreateRequest request) {
        String emailHash = emailVerificationService.consumeToken(request.signupToken());
        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(emailHash, passwordHash, request.nickname());
        userRepository.save(user);
    }

    @Transactional
    public LoginResponse login(@Valid @NotNull LoginRequest request) {
        String emailHash = emailVerificationService.hashEmail(request.email());
        User user = userRepository.findByEmailHash(emailHash).orElseThrow(LoginFailedException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new LoginFailedException();
        }

        // 로그인 가능한 계정에만 마지막 로그인 시각을 기록하고 토큰을 발급한다.
        if (userRepository.recordLogin(user.getId(), Status.ACTIVE, LocalDateTime.now()) == 0) {
            throw new LoginFailedException();
        }
        Jwt refresh = jwtProvider.issueRefreshToken(user.getId(), Boolean.TRUE.equals(request.rememberMe()));
        loginSessions.save(new LoginSession(user, refresh.getTokenValue(), refresh.getExpiresAt()));
        return new LoginResponse(jwtProvider.createAccessToken(user.getId()), refresh.getTokenValue());
    }

    @Transactional
    public void logout(String refreshToken) {
        // 발급한 토큰의 해시와 일치하는 세션만 삭제하므로 재시도해도 다른 세션에 영향을 주지 않는다.
        loginSessions.deleteByTokenHash(LoginSession.hashToken(refreshToken));
    }

}
