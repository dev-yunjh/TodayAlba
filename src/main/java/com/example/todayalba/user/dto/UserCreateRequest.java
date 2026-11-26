package com.example.todayalba.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password,
        @NotBlank
        @Size(max = 15)
        String nickname
) {
}
