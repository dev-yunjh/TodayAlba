package com.example.todayalba.user.domain;

import com.example.todayalba.user.exception.VerificationCodeException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "email_verifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {
    @Id
    @Getter
    @Column(length = 64)
    private String emailHash;

    @Version
    private Long version;

    @Getter
    @Column(length = 64)
    private String codeHash;

    private Instant codeExpiresAt;
    private Instant resendAfter;
    private int remainingAttempts;

    @Column(unique = true, length = 64)
    private String tokenHash;
    private Instant tokenExpiresAt;

    public EmailVerification(String emailHash) {
        this.emailHash = emailHash;
    }

    // 첫 요청이거나 재발송 대기 시간이 지나면 인증번호를 보낼 수 있다.
    public boolean canSendAt(Instant now) {
        return resendAfter == null || !now.isBefore(resendAfter);
    }

    // 새 인증번호의 검증값을 기록하고 시도 횟수, 만료 시각, 재발송 가능 시각을 설정한다.
    public void issueCode(String hash, Instant now) {
        codeHash = hash;
        remainingAttempts = 5;
        codeExpiresAt = now.plusSeconds(150);
        resendAfter = now.plusSeconds(30);
    }

    // 번호를 비교하기 전에 사용 가능한 인증번호가 있는지, 유효 기간과 시도 횟수가 남았는지 확인한다.
    public void ensureCodeCanBeChecked(Instant now) {
        if (codeHash == null) {
            throw VerificationCodeException.unavailable();
        }
        if (!now.isBefore(codeExpiresAt)) {
            throw VerificationCodeException.expired();
        }
        ensureAttemptsRemain();
    }

    // 인증번호가 틀리면 시도 횟수를 하나 차감하고 마지막 기회였다면 횟수 소진 예외를 던진다.
    public void rejectAttempt() {
        remainingAttempts--;
        ensureAttemptsRemain();
    }

    // 남은 시도 횟수가 0 이하면 횟수 소진 예외를 던진다.
    private void ensureAttemptsRemain() {
        if (remainingAttempts <= 0) {
            throw VerificationCodeException.attemptsExceeded();
        }
    }

    // 인증에 성공하면 번호를 다시 쓸 수 없게 하고 가입 토큰의 검증값과 만료 시각을 기록한다.
    public void confirm(String hash, Instant now) {
        remainingAttempts = 0;
        codeHash = null;
        tokenHash = hash;
        tokenExpiresAt = now.plusSeconds(300);
    }

    public boolean tokenValidAt(Instant now) {
        return tokenExpiresAt != null && now.isBefore(tokenExpiresAt);
    }
}
