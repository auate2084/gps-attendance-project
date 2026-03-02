package com.attendance.user.domain;

import com.attendance.organization.domain.RoleLevel;
import com.attendance.organization.domain.Team;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void changeRoleLevelUpdatesRoleLevel() {
        Team root = new Team("Engineering", null);
        Team team = new Team("Platform", root);
        User user = new User("u1", "pw", "u1@test.com", "U1", RoleLevel.TEAM_MEMBER, team, null);

        user.changeRoleLevel(RoleLevel.MANAGER);

        assertThat(user.getRoleLevel()).isEqualTo(RoleLevel.MANAGER);
    }

    @Test
    void changeTeamUpdatesTeam() {
        Team root = new Team("Engineering", null);
        Team teamA = new Team("A", root);
        Team teamB = new Team("B", root);
        User user = new User("u1", "pw", "u1@test.com", "U1", RoleLevel.TEAM_MEMBER, teamA, null);

        user.changeTeam(teamB);

        assertThat(user.getTeam()).isEqualTo(teamB);
    }

    @Test
    void hrAuthorityCanBeGrantedAndRevokedIndependentlyFromRoleLevel() {
        Team root = new Team("Engineering", null);
        Team team = new Team("A", root);
        User user = new User("u1", "pw", "u1@test.com", "U1", RoleLevel.TEAM_LEAD, team, null);

        user.grantHrAuthority();
        assertThat(user.isHrAuthority()).isTrue();
        assertThat(user.getRoleLevel()).isEqualTo(RoleLevel.TEAM_LEAD);

        user.revokeHrAuthority();
        assertThat(user.isHrAuthority()).isFalse();
        assertThat(user.getRoleLevel()).isEqualTo(RoleLevel.TEAM_LEAD);
    }
}
