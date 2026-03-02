package com.attendance.organization.application;

import com.attendance.attendance.domain.WorkPolicy;
import com.attendance.attendance.infrastructure.WorkPolicyRepository;
import com.attendance.organization.domain.RoleLevel;
import com.attendance.organization.domain.Team;
import com.attendance.organization.infrastructure.TeamRepository;
import com.attendance.shared.exception.BusinessException;
import com.attendance.user.domain.User;
import com.attendance.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationCommandServiceTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private WorkPolicyRepository workPolicyRepository;
    @Mock
    private UserRepository userRepository;

    private OrganizationCommandService organizationCommandService;

    @BeforeEach
    void setUp() {
        organizationCommandService = new OrganizationCommandService(teamRepository, workPolicyRepository, userRepository);
    }

    @Test
    void updateTeamChangesNameAndParentWhenActorIsHr() {
        Team root = new Team("Root", null);
        Team team = new Team("Platform", null);
        User hr = new User("hr1", "pw", "hr@test.com", "Hr", RoleLevel.TEAM_MEMBER, team, null);
        hr.grantHrAuthority();

        ReflectionTestUtils.setField(root, "id", 10L);
        ReflectionTestUtils.setField(team, "id", 100L);

        when(userRepository.findByLoginId("hr1")).thenReturn(Optional.of(hr));
        when(teamRepository.findById(100L)).thenReturn(Optional.of(team));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(root));
        when(teamRepository.existsByParentTeamAndNameAndIdNot(root, "Platform2", 100L)).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Team updated = organizationCommandService.updateTeam("hr1", 100L, "Platform2", 10L);

        assertThat(updated.getName()).isEqualTo("Platform2");
        assertThat(updated.getParentTeam()).isEqualTo(root);
    }

    @Test
    void createTeamDeniedForNonHr() {
        User lead = new User("lead1", "pw", "lead@test.com", "Lead", RoleLevel.TEAM_LEAD, null, null);
        when(userRepository.findByLoginId("lead1")).thenReturn(Optional.of(lead));

        assertThatThrownBy(() -> organizationCommandService.createTeam("lead1", null, "Engineering"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("permission denied");
    }

    @Test
    void updateTeamRejectsSelfParent() {
        Team team = new Team("Platform", null);
        User hr = new User("hr1", "pw", "hr@test.com", "Hr", RoleLevel.TEAM_MEMBER, team, null);
        hr.grantHrAuthority();
        ReflectionTestUtils.setField(team, "id", 100L);

        when(userRepository.findByLoginId("hr1")).thenReturn(Optional.of(hr));
        when(teamRepository.findById(100L)).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> organizationCommandService.updateTeam("hr1", 100L, "Platform", 100L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("team cannot be parent of itself");
    }

    @Test
    void updateTeamRejectsCycle() {
        Team root = new Team("Root", null);
        Team parent = new Team("Parent", root);
        Team child = new Team("Child", parent);
        User hr = new User("hr1", "pw", "hr@test.com", "Hr", RoleLevel.TEAM_MEMBER, parent, null);
        hr.grantHrAuthority();

        ReflectionTestUtils.setField(root, "id", 1L);
        ReflectionTestUtils.setField(parent, "id", 2L);
        ReflectionTestUtils.setField(child, "id", 3L);

        when(userRepository.findByLoginId("hr1")).thenReturn(Optional.of(hr));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(parent));
        when(teamRepository.findById(3L)).thenReturn(Optional.of(child));

        assertThatThrownBy(() -> organizationCommandService.updateTeam("hr1", 2L, "Parent", 3L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("cyclic team hierarchy is not allowed");
    }

    @Test
    void updateWorkPolicyAllowedForHr() {
        Team team = new Team("Platform", null);
        WorkPolicy policy = new WorkPolicy("Old", 37.5, 127.0, 200, 300, 10, team);
        User hr = new User("hr1", "pw", "hr@test.com", "Hr", RoleLevel.TEAM_MEMBER, team, null);
        hr.grantHrAuthority();

        ReflectionTestUtils.setField(team, "id", 10L);
        ReflectionTestUtils.setField(policy, "id", 1L);

        when(userRepository.findByLoginId("hr1")).thenReturn(Optional.of(hr));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(workPolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(workPolicyRepository.save(any(WorkPolicy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkPolicy updated = organizationCommandService.updateWorkPolicy(
                "hr1", 1L, 10L, "New", 37.55, 127.05, 250, 350, 15
        );

        assertThat(updated.getName()).isEqualTo("New");
    }

    @Test
    void updateWorkPolicyDeniedForTeamLeadWithoutHr() {
        Team team = new Team("Platform", null);
        User lead = new User("lead1", "pw", "lead@test.com", "Lead", RoleLevel.TEAM_LEAD, team, null);

        when(userRepository.findByLoginId("lead1")).thenReturn(Optional.of(lead));

        assertThatThrownBy(() -> organizationCommandService.updateWorkPolicy(
                "lead1", 1L, 10L, "New", 37.55, 127.05, 250, 350, 15
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("permission denied");
    }
}
