package com.example.albaz.user.exception;

import com.example.albaz.global.error.exception.ApiException;
import org.springframework.http.HttpStatus;

public class VerificationCodeException extends ApiException {
    private final String code;

    private VerificationCodeException(String code, String message) {
        super(message);
        this.code = code;
    }

    public static VerificationCodeException unavailable() {
        return new VerificationCodeException("VERIFICATION_CODE_UNAVAILABLE", "사용할 수 있는 인증번호가 없습니다. 새 인증번호를 요청해 주세요.");
    }

    public static VerificationCodeException expired() {
        return new VerificationCodeException("VERIFICATION_CODE_EXPIRED", "인증번호가 만료되었습니다. 새 인증번호를 요청해 주세요.");
    }

    public static VerificationCodeException mismatch() {
        return new VerificationCodeException("VERIFICATION_CODE_MISMATCH", "인증번호가 일치하지 않습니다.");
    }

    public static VerificationCodeException attemptsExceeded() {
        return new VerificationCodeException("VERIFICATION_ATTEMPTS_EXCEEDED", "인증 시도 횟수를 모두 사용했습니다. 새 인증번호를 요청해 주세요.");
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getCode() {
        return code;
    }
}
