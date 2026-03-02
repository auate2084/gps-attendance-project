package com.attendance.attendance.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record LocationUpdateRequest(
        @NotNull Double latitude,
        @NotNull Double longitude,
        LocalDateTime observedAt
) {
}
