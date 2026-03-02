package com.attendance.user.presentation;

import com.attendance.shared.security.UserSession;
import com.attendance.user.application.AuthService;
import com.attendance.user.application.UserService;
import com.attendance.user.domain.User;
import com.attendance.user.presentation.dto.LoginRequest;
import com.attendance.user.presentation.dto.RefreshTokenRequest;
import com.attendance.user.presentation.dto.RegisterUserRequest;
import com.attendance.user.presentation.dto.TokenResponse;
import com.attendance.user.presentation.dto.UpdateUserOrganizationRequest;
import com.attendance.user.presentation.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * 회원가입 요청을 처리하고 생성된 사용자 정보를 반환한다.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        User user = userService.register(
                request.loginId(),
                request.password(),
                request.email(),
                request.name(),
                request.teamId()
        );
        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * 로그인 아이디와 비밀번호를 검증한 뒤 액세스/리프레시 토큰을 발급한다.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthTokens tokens = authService.login(request.loginId(), request.password());
        return ResponseEntity.ok(new TokenResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                "Bearer",
                UserResponse.from(tokens.user())
        ));
    }

    /**
     * 유효한 리프레시 토큰으로 새로운 토큰 쌍을 재발급한다.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthService.AuthTokens tokens = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(new TokenResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                "Bearer",
                UserResponse.from(tokens.user())
        ));
    }

    @PatchMapping("/me/team")
    public ResponseEntity<UserResponse> updateTeam(
            @AuthenticationPrincipal UserSession userSession,
            @Valid @RequestBody UpdateUserOrganizationRequest request
    ) {
        User user = userService.updateMyTeam(userSession.getLoginId(), request.roleLevel(), request.teamId());
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
