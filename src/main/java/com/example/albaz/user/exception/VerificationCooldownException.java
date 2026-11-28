package com.example.albaz.user.exception;

import com.example.albaz.global.error.exception.ApiException;
import org.springframework.http.HttpStatus;

public class VerificationCooldownException extends ApiException {

    public VerificationCooldownException() {
        super("잠시 후 인증번호를 다시 요청해 주세요.");
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.TOO_MANY_REQUESTS.value();
    }

    @Override
    public String getCode() {
        return "VERIFICATION_COOLDOWN";
    }
}
