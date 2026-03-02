package com.attendance.organization.application;

import com.attendance.organization.domain.RoleLevel;
import com.attendance.organization.domain.Team;
import com.attendance.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class AttendanceAccessPolicy {

    // 조회자와 대상자의 조직/직급/팀 정보를 기준으로 세션 조회 가능 여부를 판단한다.
    public boolean canView(User viewer, User target) {
        if (viewer.getId().equals(target.getId())) {
            return true;
        }
        if (viewer.getTeam() == null || target.getTeam() == null) {
            return false;
        }
        if (!isSameOrganization(viewer.getTeam(), target.getTeam())) {
            return false;
        }
        if (viewer.getRoleLevel().isHigherThan(target.getRoleLevel())) {
            return true;
        }
        return viewer.getRoleLevel() == RoleLevel.TEAM_MEMBER
                && target.getRoleLevel() == RoleLevel.TEAM_MEMBER
                && viewer.getTeam().getId().equals(target.getTeam().getId());
    }

    // 두 팀이 같은 조직(같은 루트 팀)인지 확인한다.
    private boolean isSameOrganization(Team a, Team b) {
        Long rootA = a.rootTeamId();
        Long rootB = b.rootTeamId();
        return rootA != null && rootA.equals(rootB);
    }
}
