package com.attendance.attendance.presentation.dto;

import com.attendance.attendance.application.TrackingState;

public record LocationUpdateResponse(
        TrackingState state,
        String message,
        Long sessionId,
        Double distanceM
) {
}
