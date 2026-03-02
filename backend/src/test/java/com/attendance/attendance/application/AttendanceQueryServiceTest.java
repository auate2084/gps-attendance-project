package com.attendance.attendance.application;

import com.attendance.attendance.infrastructure.WorkSessionRepository;
import com.attendance.organization.application.AttendanceAccessPolicy;
import com.attendance.organization.domain.RoleLevel;
import com.attendance.organization.domain.Team;
import com.attendance.organization.infrastructure.TeamRepository;
import com.attendance.user.domain.User;
import com.attendance.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceQueryServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WorkSessionRepository workSessionRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private AttendanceAccessPolicy attendanceAccessPolicy;

    private AttendanceQueryService attendanceQueryService;
    private final Pageable pageable = PageRequest.of(0, 20);

    @BeforeEach
    void setUp() {
        attendanceQueryService = new AttendanceQueryService(userRepository, workSessionRepository, teamRepository, attendanceAccessPolicy);
    }

    @Test
    void teamMemberUsesSameTeamScopeForVisibleSessions() {
        Team root = new Team("Engineering", null);
        Team teamA = new Team("A", root);

        User viewer = new User("viewer", "pw", "viewer@test.com", "Viewer", RoleLevel.TEAM_MEMBER, teamA, null);
        User sameTeamTarget = new User("a2", "pw", "a2@test.com", "A2", RoleLevel.TEAM_MEMBER, teamA, null);

        setIds(root, 1L, teamA, 10L, viewer, 100L);
        setIds(root, 1L, teamA, 10L, sameTeamTarget, 101L);

        when(userRepository.findByLoginIdWithRelations("viewer")).thenReturn(Optional.of(viewer));
        when(userRepository.findByTeamId(10L)).thenReturn(List.of(viewer, sameTeamTarget));
        when(attendanceAccessPolicy.canView(viewer, viewer)).thenReturn(true);
        when(attendanceAccessPolicy.canView(viewer, sameTeamTarget)).thenReturn(true);
        when(workSessionRepository.findByUserIdInOrderByCheckInAtDesc(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertThat(attendanceQueryService.visibleSessionsByLoginId("viewer", null, null, pageable)).isEmpty();

        verify(userRepository).findByTeamId(10L);
        verify(attendanceAccessPolicy).canView(viewer, sameTeamTarget);
    }

    @Test
    void managerUsesDescendantScopeForVisibleSessions() {
        Team root = new Team("Engineering", null);
        Team viewerTeam = new Team("Platform", root);
        Team childTeam = new Team("API", viewerTeam);
        Team siblingTeam = new Team("Web", root);

        User viewer = new User("manager", "pw", "manager@test.com", "Manager", RoleLevel.MANAGER, viewerTeam, null);
        User sameTeamTarget = new User("same", "pw", "same@test.com", "Same", RoleLevel.TEAM_MEMBER, viewerTeam, null);
        User childTeamTarget = new User("child", "pw", "child@test.com", "Child", RoleLevel.TEAM_MEMBER, childTeam, null);

        setIds(root, 1L, viewerTeam, 10L, viewer, 100L);
        setIds(root, 1L, viewerTeam, 10L, sameTeamTarget, 101L);
        setIds(root, 1L, childTeam, 12L, childTeamTarget, 102L);
        ReflectionTestUtils.setField(siblingTeam, "id", 11L);

        when(userRepository.findByLoginIdWithRelations("manager")).thenReturn(Optional.of(viewer));
        when(teamRepository.findAll()).thenReturn(List.of(root, viewerTeam, childTeam, siblingTeam));
        when(userRepository.findByTeamIdIn(any())).thenReturn(List.of(viewer, sameTeamTarget, childTeamTarget));
        when(attendanceAccessPolicy.canView(viewer, viewer)).thenReturn(true);
        when(attendanceAccessPolicy.canView(viewer, sameTeamTarget)).thenReturn(true);
        when(attendanceAccessPolicy.canView(viewer, childTeamTarget)).thenReturn(true);
        when(workSessionRepository.findByUserIdInOrderByCheckInAtDesc(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertThat(attendanceQueryService.visibleSessionsByLoginId("manager", null, null, pageable)).isEmpty();

        verify(attendanceAccessPolicy).canView(viewer, sameTeamTarget);
        verify(attendanceAccessPolicy).canView(viewer, childTeamTarget);
    }

    private void setIds(Team root, Long rootId, Team team, Long teamId, User user, Long userId) {
        ReflectionTestUtils.setField(root, "id", rootId);
        ReflectionTestUtils.setField(team, "id", teamId);
        ReflectionTestUtils.setField(user, "id", userId);
    }
}
