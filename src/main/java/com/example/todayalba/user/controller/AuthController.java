package com.example.todayalba.user.controller;

import com.example.todayalba.global.common.SuccessResponse;
import com.example.todayalba.user.dto.EmailVerificationSendRequest;
import com.example.todayalba.user.dto.EmailVerificationConfirmRequest;
import com.example.todayalba.user.dto.EmailVerificationConfirmResponse;
import com.example.todayalba.user.dto.UserCreateRequest;
import com.example.todayalba.user.service.EmailVerificationService;
import com.example.todayalba.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final EmailVerificationService verifications;
    private final UserService userService;

    // 회원가입
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public SuccessResponse<Void> register(@Valid @RequestBody UserCreateRequest request) {
        userService.register(request);

        return SuccessResponse.<Void>builder()
                .status(HttpStatus.CREATED.value())
                .message("회원가입이 완료되었습니다.")
                .build();
    }

    // 이메일 인증
    @PostMapping("/email-verifications")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void send(@Valid @RequestBody EmailVerificationSendRequest request) {
        verifications.requestCode(request.email());
    }

    // 이메일 검증
    @PostMapping("/email-verifications/confirm")
    public EmailVerificationConfirmResponse confirm(@Valid @RequestBody EmailVerificationConfirmRequest request) {
        return new EmailVerificationConfirmResponse(verifications.verifyCode(request.email(), request.code()));
    }

}
