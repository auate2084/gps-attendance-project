package com.attendance.attendance.presentation.dto;

import com.attendance.attendance.domain.WorkSession;
import com.attendance.attendance.domain.WorkSessionStatus;

import java.time.LocalDateTime;

public record WorkSessionResponse(
        Long sessionId,
        Long userId,
        String userName,
        WorkSessionStatus status,
        LocalDateTime checkInAt,
        LocalDateTime checkOutAt,
        LocalDateTime outsideSince
) {
    // WorkSession 도메인 객체를 API 응답 DTO로 변환한다.
    public static WorkSessionResponse from(WorkSession session) {
        return new WorkSessionResponse(
                session.getId(),
                session.getUser().getId(),
                session.getUser().getName(),
                session.getStatus(),
                session.getCheckInAt(),
                session.getCheckOutAt(),
                session.getOutsideSince()
        );
    }
}
