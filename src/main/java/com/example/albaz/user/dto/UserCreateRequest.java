package com.example.albaz.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank
        @Pattern(regexp = "[a-f0-9]{64}")
        String signupToken,

        @NotBlank
        @Size(min = 8, max = 64, message = "비밀번호는 8~64자로 입력해 주세요.")
        @Pattern(
                regexp = "(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#$%^&*()=_+\\-])[A-Za-z0-9!@#$%^&*()=_+\\-]+",
                message = "비밀번호는 영문, 숫자, 특수문자(!@#$%^&*()-=_+)를 각각 하나 이상 포함해 주세요."
        )
        String password,

        @NotBlank
        @Size(max = 15)
        @Pattern(regexp = "[가-힣A-Za-z0-9]+", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있어요.")
        String nickname
) {
}
