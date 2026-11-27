package com.example.todayalba.global.error;

import com.example.todayalba.global.common.FailResponse;
import com.example.todayalba.global.error.exception.ApiException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<FailResponse> handleApiException(ApiException ex) {
        int status = ex.getStatusCode();
        return ResponseEntity.status(status)
                .body(new FailResponse(status, ex.getCode(), ex.getMessage(), Map.of()));
    }
}
