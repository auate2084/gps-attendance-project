package com.attendance.user.infrastructure;

import com.attendance.user.application.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String TOKEN_KEY_PREFIX = "auth:refresh:token:";
    private static final String USER_KEY_PREFIX = "auth:refresh:user:";

    private final StringRedisTemplate redisTemplate;

    @Override
    // 사용자 기준 기존 토큰을 정리한 뒤 토큰-사용자 매핑을 TTL과 함께 저장한다.
    public void save(Long userId, String token, LocalDateTime expiresAt) {
        Duration ttl = Duration.between(LocalDateTime.now(ZoneOffset.UTC), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }

        String userKey = userKey(userId);
        String oldToken = redisTemplate.opsForValue().get(userKey);
        if (oldToken != null) {
            redisTemplate.delete(tokenKey(oldToken));
        }

        redisTemplate.opsForValue().set(tokenKey(token), String.valueOf(userId), ttl);
        redisTemplate.opsForValue().set(userKey, token, ttl);
    }

    @Override
    // 토큰 문자열로 저장소를 조회해 사용자 ID를 반환한다.
    public Optional<Long> findUserIdByToken(String token) {
        String value = redisTemplate.opsForValue().get(tokenKey(token));
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(Long.parseLong(value));
    }

    @Override
    // 토큰을 기준으로 토큰 키와 사용자 키의 연결 정보를 함께 폐기한다.
    public void revokeByToken(String token) {
        String tokenKey = tokenKey(token);
        String userId = redisTemplate.opsForValue().get(tokenKey);
        redisTemplate.delete(tokenKey);
        if (userId == null) {
            return;
        }
        String userKey = userKey(Long.parseLong(userId));
        String currentToken = redisTemplate.opsForValue().get(userKey);
        if (token.equals(currentToken)) {
            redisTemplate.delete(userKey);
        }
    }

    @Override
    // 사용자 ID 기준으로 현재 리프레시 토큰과 매핑 정보를 폐기한다.
    public void revokeByUserId(Long userId) {
        String userKey = userKey(userId);
        String token = redisTemplate.opsForValue().get(userKey);
        redisTemplate.delete(userKey);
        if (token != null) {
            redisTemplate.delete(tokenKey(token));
        }
    }

    // 리프레시 토큰 저장용 Redis 키를 생성한다.
    private String tokenKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }

    // 사용자-토큰 매핑 저장용 Redis 키를 생성한다.
    private String userKey(Long userId) {
        return USER_KEY_PREFIX + userId;
    }
}
