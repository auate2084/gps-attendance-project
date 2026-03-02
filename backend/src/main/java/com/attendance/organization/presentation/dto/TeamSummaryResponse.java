package com.attendance.organization.presentation.dto;

import com.attendance.organization.domain.Team;

public record TeamSummaryResponse(
        Long id,
        String name
) {
    public static TeamSummaryResponse from(Team team) {
        return new TeamSummaryResponse(team.getId(), team.getName());
    }
}
