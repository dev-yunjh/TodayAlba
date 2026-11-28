package com.example.albaz.global.error.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public abstract class ApiException extends RuntimeException {

    protected ApiException(String message) {
        super(message);
    }

    public abstract int getStatusCode();
    public abstract String getCode();
}
