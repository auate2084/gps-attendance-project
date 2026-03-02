package com.attendance.user.application;

import com.attendance.shared.exception.BusinessException;
import com.attendance.shared.security.JwtTokenProvider;
import com.attendance.user.domain.User;
import com.attendance.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RefreshTokenStore refreshTokenStore;
    private final JwtTokenProvider jwtTokenProvider;

    // 사용자 로그인을 처리하고 신규 액세스/리프레시 토큰을 발급한다.
    public AuthTokens login(String loginId, String password) {
        User user = userService.login(loginId, password);
        return issueTokens(user, true);
    }

    // 리프레시 토큰을 검증한 후 기존 토큰을 폐기하고 새 토큰 쌍을 발급한다.
    public AuthTokens refresh(String refreshTokenValue) {
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            throw new BusinessException("invalid refresh token");
        }
        if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshTokenValue))) {
            throw new BusinessException("invalid refresh token type");
        }

        Long userId = refreshTokenStore.findUserIdByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException("refresh token not found"));
        if (!userId.equals(jwtTokenProvider.getUserId(refreshTokenValue))) {
            throw new BusinessException("invalid refresh token");
        }
        refreshTokenStore.revokeByToken(refreshTokenValue);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("user not found"));

        return issueTokens(user, false);
    }

    // 토큰 발급과 저장소 동기화를 수행해 인증 토큰 결과를 생성한다.
    private AuthTokens issueTokens(User user, boolean revokeOld) {
        if (revokeOld) {
            refreshTokenStore.revokeByUserId(user.getId());
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user);
        LocalDateTime refreshExpireAt = LocalDateTime.ofInstant(jwtTokenProvider.getExpiration(refreshTokenValue), ZoneOffset.UTC);

        refreshTokenStore.save(user.getId(), refreshTokenValue, refreshExpireAt);

        return new AuthTokens(user, accessToken, refreshTokenValue);
    }

    public record AuthTokens(
            User user,
            String accessToken,
            String refreshToken
    ) {
    }
}
