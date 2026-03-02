package com.attendance.user.presentation.dto;

import com.attendance.organization.domain.RoleLevel;
import jakarta.validation.constraints.NotNull;

public record UpdateUserOrganizationRequest(
        @NotNull RoleLevel roleLevel,
        @NotNull Long teamId
) {
}
