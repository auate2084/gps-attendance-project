package com.attendance.user.application;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenStore {
    // 사용자 ID와 토큰을 만료 시각과 함께 저장한다.
    void save(Long userId, String token, LocalDateTime expiresAt);
    // 토큰으로 사용자 ID를 조회한다.
    Optional<Long> findUserIdByToken(String token);
    // 토큰 기준으로 저장된 인증 정보를 폐기한다.
    void revokeByToken(String token);
    // 사용자 기준으로 저장된 인증 정보를 폐기한다.
    void revokeByUserId(Long userId);
}
