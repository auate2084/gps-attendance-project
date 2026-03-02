package com.attendance.organization.presentation.dto;

import com.attendance.organization.domain.Team;

public record TeamResponse(
        Long id,
        String name,
        Long parentTeamId
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getParentTeam() == null ? null : team.getParentTeam().getId()
        );
    }
}
