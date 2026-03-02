package com.attendance.organization.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTeamRequest(
        Long parentTeamId,
        @NotBlank String name
) {
}
