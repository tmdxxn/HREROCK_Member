package com.movie.rock.config;

import lombok.Data;

// 김승준 - 회원
@Data
public class TokenRefreshRequest {
    
    // 토큰 리프래쉬를 위해
    private String refreshToken;
}
