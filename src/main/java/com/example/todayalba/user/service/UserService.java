package com.example.todayalba.user.service;

import com.example.todayalba.user.domain.User;
import com.example.todayalba.user.dto.UserCreateRequest;
import com.example.todayalba.user.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Transactional
    public void register(@Valid @NotNull UserCreateRequest request) {
        String emailHash = emailVerificationService.consumeToken(request.signupToken());
        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(emailHash, passwordHash, request.nickname());
        userRepository.save(user);
    }

}
