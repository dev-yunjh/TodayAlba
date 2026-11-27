package com.example.todayalba.user.service;

import com.example.todayalba.user.domain.User;
import com.example.todayalba.user.dto.UserCreateRequest;
import com.example.todayalba.user.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
    private final SecretKeySpec emailHashKey;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(@Valid @NotNull UserCreateRequest request) {
        String emailHash = hashEmail(request.email());
        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(emailHash, passwordHash, request.nickname());
        userRepository.save(user);
    }

    private String hashEmail(String email) {
        try {
            Mac mac = Mac.getInstance(emailHashKey.getAlgorithm());
            mac.init(emailHashKey);
            String normalizedEmail = email.toLowerCase(Locale.ROOT);
            return HexFormat.of().formatHex(mac.doFinal(normalizedEmail.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Cannot compute email hash", ex);
        }
    }

}
