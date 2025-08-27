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

    @Value("${jwt.secret}")               // 최소 32바이트 이상 (권장: Base64 256bit)
    private String secretKey;

    @Value("${jwt.expiration-in-ms}")
    private long expirationInMs;

    private Key key;

    @PostConstruct
    public void init() {
        // ⚠️ secretKey가 너무 짧으면 HS256에서 WeakKeyException 발생
        // 권장: 32바이트 이상 or Base64로 256bit 제공
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // JWT 생성
    public String createToken(String loginId, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInMs);

        return Jwts.builder()
                .setSubject(loginId)            // sub = loginId (유지)
                .claim("userId", userId)        // uid 클레임
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // loginId(subject) 추출 (현재 필터에서 이미 사용 중)
    public String getLoginIdFromToken(String token) {
        return getAllClaims(token).getSubject();
    }

    // ✅ userId 추출 — 필터에서 이걸로 로딩하면 더 안정적
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaims(token);
        Object val = claims.get("userId");
        if (val instanceof Integer i) return i.longValue();
        if (val instanceof Long l) return l;
        if (val instanceof String s) return Long.valueOf(s);
        throw new IllegalArgumentException("Invalid userId claim type: " + (val == null ? "null" : val.getClass()));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 만료: 필요하면 로그/모니터링
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();
    }
}
