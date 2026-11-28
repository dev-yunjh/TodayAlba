package com.example.albaz.user.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {}
