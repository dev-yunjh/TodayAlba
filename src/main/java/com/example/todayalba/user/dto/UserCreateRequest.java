package com.example.todayalba.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank
        @Pattern(regexp = "[a-f0-9]{64}")
        String signupToken,
        @NotBlank
        String password,
        @NotBlank
        @Size(max = 15)
        String nickname
) {
}
