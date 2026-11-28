package com.example.albaz.user.service;

import com.example.albaz.user.domain.EmailVerification;
import com.example.albaz.user.exception.VerificationCodeException;
import com.example.albaz.user.exception.VerificationCooldownException;
import com.example.albaz.user.mail.VerificationMailSender;
import com.example.albaz.user.repository.EmailVerificationRepository;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final EmailVerificationRepository verifications;
    private final VerificationMailSender mailSender;
    private final SecretKeySpec emailHashKey;
    private final SecureRandom random = new SecureRandom();

    // 이메일 인증번호 전송
    @Transactional
    public void requestCode(String email) {
        String address = email.toLowerCase(Locale.ROOT);
        String emailHash = hmac(address);

        EmailVerification verification = verifications.findByEmailHash(emailHash)
                .orElseGet(() -> new EmailVerification(emailHash));

        Instant now = Instant.now();

        if (!verification.canSendAt(now)) {
            throw new VerificationCooldownException();
        }

        String code = String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
        verification.issueCode(codeHash(emailHash, code), now);
        verifications.saveAndFlush(verification);
        mailSender.sendCode(address, code);
    }

    // 검증 실패를 응답하더라도 차감한 시도 횟수는 커밋한다.
    @Transactional(noRollbackFor = VerificationCodeException.class)
    public String verifyCode(String email, String code) {

        String emailHash = hmac(email.toLowerCase(Locale.ROOT));

        EmailVerification verification = verifications.findByEmailHash(emailHash)
                .orElseThrow(VerificationCodeException::unavailable);

        Instant now = Instant.now();

        verification.ensureCodeCanBeChecked(now);

        // 발송할 때 저장한 검증값을 16진수 문자열에서 바이트 배열로 변환한다.
        byte[] expected = HexFormat.of().parseHex(verification.getCodeHash());
        // 사용자가 입력한 번호로 같은 방식의 검증값을 계산한다.
        byte[] actual = HexFormat.of().parseHex(codeHash(emailHash, code));

        // 두 값이 다르면 남은 시도 횟수를 하나 줄이고 인증 실패를 반환한다.
        if (!MessageDigest.isEqual(expected, actual)) {
            verification.rejectAttempt();
            throw VerificationCodeException.mismatch();
        }

        // 번호가 일치하면 무작위 32바이트를 생성해 64자리 가입 토큰으로 표현한다.
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);

        // 인증번호를 사용 처리하고, 가입 토큰의 HMAC과 만료 시각을 기록한다.
        verification.confirm(hmac("signup-token:" + token), now);

        // 다음 회원가입 요청에 사용할 토큰 원문을 반환한다.
        return token;
    }

    @Transactional
    public String consumeToken(String token) {
        EmailVerification verification = verifications.findByTokenHash(hmac("signup-token:" + token))
                .filter(entry -> entry.tokenValidAt(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid signup token"));
        verifications.delete(verification);
        return verification.getEmailHash();
    }

    private String codeHash(String emailHash, String code) {
        return hmac("signup-code:" + emailHash + ":" + code);
    }

    private String hmac(String value) {
        try {
            Mac mac = Mac.getInstance(emailHashKey.getAlgorithm());
            mac.init(emailHashKey);
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Cannot compute HMAC", ex);
        }
    }

}
