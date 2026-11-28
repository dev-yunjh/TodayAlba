package com.example.albaz.user.exception;

import com.example.albaz.global.error.exception.ApiException;
import org.springframework.http.HttpStatus;

public class LoginFailedException extends ApiException {
    public LoginFailedException() {
        super("이메일 또는 비밀번호를 확인해 주세요.");
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.UNAUTHORIZED.value();
    }

    @Override
    public String getCode() {
        return "LOGIN_FAILED";
    }
}
