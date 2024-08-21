package com.movie.rock.member.data;

import lombok.Data;

// 김승준 - 회원
@Data
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String loginMethod;

    public AuthResponseDTO(String accessToken, String refreshToken, String loginMethod) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.loginMethod = loginMethod;
    }
}
