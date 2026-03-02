package com.attendance.shared.security;

import com.attendance.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    // JWT 서명 키와 토큰 만료 시간을 설정한다.
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpirationMs
    ) {
        this.key = createKey(secret);
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    // 사용자 정보를 기반으로 액세스 토큰을 생성한다.
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getLoginId())
                .claim("uid", user.getId())
                .claim("role", user.getRoleLevel().name())
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpirationMs)))
                .signWith(key)
                .compact();
    }

    // 사용자 정보를 기반으로 리프레시 토큰을 생성한다.
    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getLoginId())
                .claim("uid", user.getId())
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshTokenExpirationMs)))
                .signWith(key)
                .compact();
    }

    // 전달된 JWT의 서명과 만료 여부를 검증한다.
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰의 subject 클레임에서 로그인 아이디를 추출한다.
    public String getLoginId(String token) {
        return getClaims(token).getSubject();
    }

    // 토큰의 uid 클레임을 Long 타입 사용자 ID로 변환해 반환한다.
    public Long getUserId(String token) {
        Object uid = getClaims(token).get("uid");
        if (uid instanceof Integer i) {
            return i.longValue();
        }
        if (uid instanceof Long l) {
            return l;
        }
        return Long.parseLong(String.valueOf(uid));
    }

    // 토큰의 type 클레임(access/refresh)을 반환한다.
    public String getTokenType(String token) {
        return String.valueOf(getClaims(token).get("type"));
    }

    // 토큰 만료 시각을 Instant 형태로 반환한다.
    public Instant getExpiration(String token) {
        return getClaims(token).getExpiration().toInstant();
    }

    // 서명 검증 후 토큰 페이로드 클레임을 가져온다.
    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    // Base64 또는 일반 문자열 시크릿으로 HMAC 키를 생성한다.
    private SecretKey createKey(String secret) {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception ignored) {
            byte[] rawBytes = secret.getBytes(StandardCharsets.UTF_8);
            return Keys.hmacShaKeyFor(rawBytes);
        }
    }
}
