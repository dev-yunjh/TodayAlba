package com.example.todayalba.user.service;

import com.example.todayalba.user.domain.User;
import com.example.todayalba.user.dto.UserCreateRequest;
import com.example.todayalba.user.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class UserService {
    private final UserRepository userRepository;
    private final SecretKeySpec emailHashKey;
    private final PasswordEncoder passwordEncoder = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public UserService(UserRepository userRepository, @Value("${app.email-hash-key}") String encodedKey) {
        this.userRepository = userRepository;
        this.emailHashKey = new SecretKeySpec(Base64.getDecoder().decode(encodedKey), "HmacSHA256");
    }

    @Transactional
    public UUID register(@Valid @NotNull UserCreateRequest request) {
        String emailHash = hashEmail(request.email());
        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(emailHash, passwordHash, request.nickname());
        return userRepository.save(user).getId();
    }

    private String hashEmail(String email) {
        try {
            Mac mac = Mac.getInstance(emailHashKey.getAlgorithm());
            mac.init(emailHashKey);
            byte[] normalizedEmail = email.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().formatHex(mac.doFinal(normalizedEmail));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Cannot compute email hash", ex);
        }
    }
}
