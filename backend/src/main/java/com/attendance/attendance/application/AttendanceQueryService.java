package com.attendance.attendance.application;

import com.attendance.attendance.domain.WorkSession;
import com.attendance.attendance.infrastructure.WorkSessionRepository;
import com.attendance.organization.application.AttendanceAccessPolicy;
import com.attendance.organization.domain.Team;
import com.attendance.organization.infrastructure.TeamRepository;
import com.attendance.shared.exception.BusinessException;
import com.attendance.user.domain.User;
import com.attendance.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceQueryService {

    private final UserRepository userRepository;
    private final WorkSessionRepository workSessionRepository;
    private final TeamRepository teamRepository;
    private final AttendanceAccessPolicy attendanceAccessPolicy;

    public Page<WorkSession> mySessions(Long userId, Pageable pageable) {
        return workSessionRepository.findByUserIdOrderByCheckInAtDesc(userId, pageable);
    }

    public Page<WorkSession> visibleSessionsByLoginId(String viewerLoginId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        User viewer = userRepository.findByLoginIdWithRelations(viewerLoginId)
                .orElseThrow(() -> new BusinessException("viewer user not found"));

        if (viewer.getTeam() == null) {
            return Page.empty(pageable);
        }

        List<User> candidates = loadCandidatesByScope(viewer);

        List<Long> authorizedUserIds = candidates.stream()
                .filter(target -> attendanceAccessPolicy.canView(viewer, target))
                .map(User::getId)
                .toList();

        if (authorizedUserIds.isEmpty()) {
            return Page.empty(pageable);
        }

        if (from != null && to != null) {
            return workSessionRepository.findByUserIdInAndCheckInAtBetweenOrderByCheckInAtDesc(
                    authorizedUserIds, from, to, pageable);
        }
        return workSessionRepository.findByUserIdInOrderByCheckInAtDesc(authorizedUserIds, pageable);
    }

    private List<User> loadCandidatesByScope(User viewer) {
        return switch (viewer.getRoleLevel().visibilityScope()) {
            case SAME_TEAM -> userRepository.findByTeamId(viewer.getTeam().getId());
            case TEAM_AND_DESCENDANTS -> {
                Set<Long> descendantTeamIds = findDescendantTeamIds(viewer.getTeam().getId());
                yield userRepository.findByTeamIdIn(new ArrayList<>(descendantTeamIds));
            }
        };
    }

    private Set<Long> findDescendantTeamIds(Long baseTeamId) {
        List<Team> allTeams = teamRepository.findAll();
        Set<Long> result = new HashSet<>();
        result.add(baseTeamId);

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Team team : allTeams) {
                if (team.getParentTeam() != null
                        && result.contains(team.getParentTeam().getId())
                        && result.add(team.getId())) {
                    changed = true;
                }
            }
        }
        return result;
    }
}
