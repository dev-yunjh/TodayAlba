package com.example.albaz.user.controller;

import com.example.albaz.global.common.SuccessResponse;
import com.example.albaz.user.dto.EmailVerificationSendRequest;
import com.example.albaz.user.dto.EmailVerificationConfirmRequest;
import com.example.albaz.user.dto.EmailVerificationConfirmResponse;
import com.example.albaz.user.dto.UserCreateRequest;
import com.example.albaz.user.dto.LoginRequest;
import com.example.albaz.user.dto.LoginResponse;
import com.example.albaz.user.dto.LogoutRequest;
import com.example.albaz.user.service.EmailVerificationService;
import com.example.albaz.user.service.UserService;
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

    @PostMapping("/login")
    public SuccessResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse tokens = userService.login(request);

        return SuccessResponse.<LoginResponse>builder()
                .status(HttpStatus.OK.value())
                .message("로그인 성공")
                .data(tokens)
                .build();
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        userService.logout(request.refreshToken());
    }

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
