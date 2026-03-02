package com.attendance.organization.application;

import com.attendance.organization.domain.RoleLevel;
import com.attendance.organization.domain.Team;
import com.attendance.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceAccessPolicyTest {

    private final AttendanceAccessPolicy policy = new AttendanceAccessPolicy();

    @Test
    // 같은 조직에서 상위 권한자가 하위 권한자를 조회할 수 있는지 검증한다.
    void departmentHeadCanViewLowerRankInSameOrganization() {
        Team root = new Team("Engineering", null);
        Team team = new Team("Platform", root);
        User head = new User("head1", "pw", "head@test.com", "Head", RoleLevel.DEPARTMENT_HEAD, root, null);
        User manager = new User("mgr1", "pw", "mgr@test.com", "Manager", RoleLevel.MANAGER, team, null);

        setIds(root, 10L, null, null, head, 1L);
        setIds(root, 10L, team, 100L, manager, 2L);

        assertThat(policy.canView(head, manager)).isTrue();
    }

    @Test
    // 하위 권한자가 상위 권한자를 조회할 수 없는지 검증한다.
    void managerCannotViewTeamLead() {
        Team root = new Team("Engineering", null);
        Team team = new Team("Platform", root);
        User manager = new User("mgr1", "pw", "mgr@test.com", "Manager", RoleLevel.MANAGER, team, null);
        User lead = new User("lead1", "pw", "lead@test.com", "Lead", RoleLevel.TEAM_LEAD, team, null);

        setIds(root, 10L, team, 100L, manager, 1L);
        setIds(root, 10L, team, 100L, lead, 2L);

        assertThat(policy.canView(manager, lead)).isFalse();
    }

    @Test
    // 팀원은 같은 팀 팀원만 조회 가능한지 검증한다.
    void teamMembersCanViewOnlySameTeamMembers() {
        Team root = new Team("Engineering", null);
        Team teamA = new Team("A", root);
        Team teamB = new Team("B", root);

        User memberA = new User("a", "pw", "a@test.com", "A", RoleLevel.TEAM_MEMBER, teamA, null);
        User memberA2 = new User("a2", "pw", "a2@test.com", "A2", RoleLevel.TEAM_MEMBER, teamA, null);
        User memberB = new User("b", "pw", "b@test.com", "B", RoleLevel.TEAM_MEMBER, teamB, null);

        setIds(root, 10L, teamA, 100L, memberA, 1L);
        setIds(root, 10L, teamA, 100L, memberA2, 2L);
        setIds(root, 10L, teamB, 101L, memberB, 3L);

        assertThat(policy.canView(memberA, memberA2)).isTrue();
        assertThat(policy.canView(memberA, memberB)).isFalse();
    }

    // 테스트 객체들에 Reflection으로 ID 값을 주입한다.
    private void setIds(Team root, Long rootId, Team team, Long teamId, User user, Long userId) {
        ReflectionTestUtils.setField(root, "id", rootId);
        if (team != null) {
            ReflectionTestUtils.setField(team, "id", teamId);
        }
        ReflectionTestUtils.setField(user, "id", userId);
    }
}
