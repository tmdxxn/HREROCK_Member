package com.movie.rock.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.function.Function;

// 김승준 - 회원
@Component
public class JwtUtil implements Serializable {

    // 시크릿.프로퍼티에 있는 값
    @Value("${jwt.secret}")
    private String secretKey;

    // 액세스 토큰 ( 3시간 유효 )
    public String generateAccessToken(String memId) {
        return createToken(memId, 1000 * 60 * 60 * 3);
    }

    // 리프래쉬 토큰 ( 7일 유효 )
    public String generateRefreshToken(String memId) {
        return createToken(memId, 1000 * 60 * 60 * 24 * 7);
    }

    // JWT토큰 생성 메서드
    private String createToken(String memId, long expirationTime) {
        return Jwts.builder()
                .setSubject(memId) // 토큰 제목 = 회원ID
                .setIssuedAt(new Date()) // 토큰 발행 시간
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 만료 시간
                .signWith(SignatureAlgorithm.HS256, secretKey) // HS256 알고리즘 + 시크릿키 서명
                .compact(); // 생성
    }

    // 토큰에서 회원ID 추출
    public String extractMemberId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 토큰에서 특정 클레임 추출
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 토큰에서 모든 클레임 추출
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    // 토큰 유효성 검사
    public boolean isTokenValid(String token, String memId) {
        final String extractedMemberId = extractMemberId(token);
        return (extractedMemberId.equals(memId) && !isTokenExpired(token));
    }

    // 토큰 만료 여부 검사
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }


    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
