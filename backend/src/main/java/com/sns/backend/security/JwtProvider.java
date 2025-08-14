package com.sns.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}") // application.properties에 설정한 비밀키
    private String secretKey;

    @Value("${jwt.expiration-in-ms}")
    private long expirationInMs;

    private Key key;

    @PostConstruct
    public void init() {
        // UTF-8 인코딩 명시 & 안전하게 Key 객체로 변환
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // JWT 토큰 생성
    public String createToken(String loginId, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInMs);

        return Jwts.builder()
                .setSubject(loginId)            // 토큰 제목 (주로 사용자 식별값)
                .claim("userId", userId)        // 커스텀 클레임 추가 가능
                .setIssuedAt(now)               // 토큰 발행 시간
                .setExpiration(expiryDate)     // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 서명 알고리즘 및 키 설정
                .compact();
    }

    // JWT 토큰에서 로그인ID 추출
    public String getLoginIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰이 유효하지 않음 (만료, 위조 등)
            return false;
        }
    }
}
