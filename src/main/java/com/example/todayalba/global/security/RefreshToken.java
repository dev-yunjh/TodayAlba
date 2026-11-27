package com.example.todayalba.global.security;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RefreshToken {
    private String header;
    private String data;

    @Builder
    public RefreshToken(String header, String data) {
        this.header = header;
        this.data = data;
    }

}
