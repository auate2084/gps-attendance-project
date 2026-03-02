package com.attendance.user.presentation.dto;

import com.attendance.user.domain.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String loginId,
        String email,
        String name,
        String roleLevel,
        Boolean hrAuthority,
        Boolean active,
        Long teamId,
        Long workPolicyId,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
    // User 도메인 객체를 API 응답용 사용자 DTO로 변환한다.
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getLoginId(),
                user.getEmail(),
                user.getName(),
                user.getRoleLevel().name(),
                user.isHrAuthority(),
                user.isActive(),
                user.getTeam() == null ? null : user.getTeam().getId(),
                user.getWorkPolicy() == null ? null : user.getWorkPolicy().getId(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
