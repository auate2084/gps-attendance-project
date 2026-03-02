package com.attendance.user.presentation.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UserResponse user
) {
}
