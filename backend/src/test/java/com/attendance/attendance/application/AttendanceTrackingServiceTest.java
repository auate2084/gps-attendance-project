package com.attendance.attendance.application;

import com.attendance.attendance.domain.WorkPolicy;
import com.attendance.attendance.domain.WorkSession;
import com.attendance.attendance.domain.WorkSessionStatus;
import com.attendance.attendance.infrastructure.WorkPolicyRepository;
import com.attendance.attendance.infrastructure.WorkSessionRepository;
import com.attendance.organization.domain.RoleLevel;
import com.attendance.organization.domain.Team;
import com.attendance.shared.geo.GeoDistanceCalculator;
import com.attendance.user.domain.User;
import com.attendance.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceTrackingServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WorkSessionRepository workSessionRepository;
    @Mock
    private WorkPolicyRepository workPolicyRepository;

    private AttendanceTrackingService attendanceTrackingService;

    @BeforeEach
    // 테스트마다 AttendanceTrackingService와 Mock 의존성을 초기화한다.
    void setUp() {
        attendanceTrackingService = new AttendanceTrackingService(
                userRepository,
                workSessionRepository,
                workPolicyRepository,
                new GeoDistanceCalculator()
        );
    }

    @Test
    // 출근 반경 내 위치 보고 시 자동 출근 세션이 시작되는지 검증한다.
    void startsCheckInWhenInsideCheckinRadius() {
        User user = createUserWithPolicy();
        LocalDateTime now = LocalDateTime.of(2026, 2, 13, 9, 0);

        when(userRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(user));
        when(workSessionRepository.findFirstByUserIdAndStatusOrderByCheckInAtDesc(1L, WorkSessionStatus.CHECKED_IN))
                .thenReturn(Optional.empty());
        when(workSessionRepository.save(any(WorkSession.class))).thenAnswer(invocation -> {
            WorkSession session = invocation.getArgument(0);
            ReflectionTestUtils.setField(session, "id", 99L);
            return session;
        });

        AttendanceTrackingService.TrackingResult result =
                attendanceTrackingService.processLocation(1L, 37.5001, 127.0001, now);

        assertThat(result.state()).isEqualTo(TrackingState.CHECKED_IN_STARTED);
        assertThat(result.sessionId()).isEqualTo(99L);
    }

    @Test
    // 유예 시간 이상 반경 이탈 시 자동 퇴근 처리되는지 검증한다.
    void checksOutAutomaticallyWhenOutsideLongerThanGraceMinutes() {
        User user = createUserWithPolicy();
        LocalDateTime firstOutside = LocalDateTime.of(2026, 2, 13, 18, 0);
        WorkSession active = new WorkSession(user, user.getWorkPolicy(), firstOutside.minusHours(8), 37.5, 127.0);
        ReflectionTestUtils.setField(active, "id", 10L);
        active.markOutside(firstOutside);

        when(userRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(user));
        when(workSessionRepository.findFirstByUserIdAndStatusOrderByCheckInAtDesc(1L, WorkSessionStatus.CHECKED_IN))
                .thenReturn(Optional.of(active));
        when(workSessionRepository.save(any(WorkSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceTrackingService.TrackingResult result =
                attendanceTrackingService.processLocation(1L, 37.5100, 127.0200, firstOutside.plusMinutes(11));

        ArgumentCaptor<WorkSession> captor = ArgumentCaptor.forClass(WorkSession.class);
        verify(workSessionRepository).save(captor.capture());

        assertThat(result.state()).isEqualTo(TrackingState.CHECKED_OUT_AUTOMATIC);
        assertThat(captor.getValue().getStatus()).isEqualTo(WorkSessionStatus.CHECKED_OUT);
        assertThat(captor.getValue().getCheckOutAt()).isEqualTo(firstOutside.plusMinutes(11));
    }

    // 위치 추적 테스트에 사용할 팀/정책/사용자 기본 객체를 생성한다.
    private User createUserWithPolicy() {
        Team root = new Team("Engineering", null);
        Team team = new Team("Platform", root);
        WorkPolicy policy = new WorkPolicy("HQ", 37.5, 127.0, 200, 300, 10, team);
        User user = new User("tester1", "pw", "tester@test.com", "Tester", RoleLevel.TEAM_MEMBER, team, policy);

        ReflectionTestUtils.setField(root, "id", 1L);
        ReflectionTestUtils.setField(team, "id", 2L);
        ReflectionTestUtils.setField(policy, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}
