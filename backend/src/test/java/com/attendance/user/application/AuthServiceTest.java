package com.attendance.user.application;

import com.attendance.organization.domain.RoleLevel;
import com.attendance.shared.exception.BusinessException;
import com.attendance.shared.security.JwtTokenProvider;
import com.attendance.user.domain.User;
import com.attendance.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenStore refreshTokenStore;

    private AuthService authService;
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    // 테스트용 JwtTokenProvider와 AuthService를 초기화한다.
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "gps-attendance-secret-key-for-jwt-token-generation-must-be-long-enough",
                1800000,
                604800000
        );
        authService = new AuthService(userService, userRepository, refreshTokenStore, jwtTokenProvider);
    }

    @Test
    // 로그인 성공 시 access/refresh 토큰이 모두 발급되는지 검증한다.
    void loginIssuesAccessAndRefreshTokens() {
        User user = createUser();
        when(userService.login("tester1", "password123!")).thenReturn(user);

        AuthService.AuthTokens tokens = authService.login("tester1", "password123!");

        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(jwtTokenProvider.getTokenType(tokens.accessToken())).isEqualTo("access");
        assertThat(jwtTokenProvider.getTokenType(tokens.refreshToken())).isEqualTo("refresh");
    }

    @Test
    // 저장소에 없는 리프레시 토큰으로 갱신 시 예외가 발생하는지 검증한다.
    void refreshFailsWhenTokenIsNotInStore() {
        User user = createUser();
        String refresh = jwtTokenProvider.generateRefreshToken(user);
        when(refreshTokenStore.findUserIdByToken(refresh)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(refresh))
                .isInstanceOf(BusinessException.class)
                .hasMessage("refresh token not found");
    }

    // 토큰 생성/검증 테스트에 사용할 기본 사용자 객체를 만든다.
    private User createUser() {
        User user = new User(
                "tester1",
                "encoded-password",
                "tester@test.com",
                "Tester",
                RoleLevel.TEAM_MEMBER,
                null,
                null
        );
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}
