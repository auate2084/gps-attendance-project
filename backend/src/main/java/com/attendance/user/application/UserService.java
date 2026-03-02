package com.attendance.user.application;

import com.attendance.attendance.domain.WorkPolicy;
import com.attendance.attendance.infrastructure.WorkPolicyRepository;
import com.attendance.organization.domain.RoleLevel;
import com.attendance.organization.domain.Team;
import com.attendance.organization.infrastructure.TeamRepository;
import com.attendance.shared.exception.BusinessException;
import com.attendance.user.domain.User;
import com.attendance.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final WorkPolicyRepository workPolicyRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(
            String loginId,
            String rawPassword,
            String email,
            String name,
            Long teamId
    ) {
        if (userRepository.existsByLoginId(loginId)) {
            throw new BusinessException("login id already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("email already exists");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("team not found"));
        WorkPolicy policy = workPolicyRepository.findFirstByTeamIdOrderByIdDesc(teamId)
                .orElse(null);

        User user = new User(
                loginId,
                passwordEncoder.encode(rawPassword),
                email,
                name,
                RoleLevel.TEAM_MEMBER,
                team,
                policy
        );
        return userRepository.save(user);
    }

    public User login(String loginId, String rawPassword) {
        User user = userRepository.findByLoginIdWithRelations(loginId)
                .orElseThrow(() -> new BusinessException("invalid login id or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BusinessException("invalid login id or password");
        }
        if (!user.isActive()) {
            throw new BusinessException("inactive account");
        }

        user.markLoginSuccess(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException("user not found"));
    }

    public User updateMyTeam(String loginId, RoleLevel roleLevel, Long teamId) {
        User user = userRepository.findByLoginIdWithRelations(loginId)
                .orElseThrow(() -> new BusinessException("user not found"));

        if (roleLevel.isHigherThan(user.getRoleLevel()) && !user.isHrAuthority()) {
            throw new BusinessException("cannot elevate own role level without HR authority");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("team not found"));

        if (user.getWorkPolicy() != null && !isSameOrganization(user.getWorkPolicy().getTeam(), team)) {
            throw new BusinessException("policy does not belong to team organization");
        }

        user.changeRoleLevel(roleLevel);
        user.changeTeam(team);
        return userRepository.save(user);
    }

    private boolean isSameOrganization(Team a, Team b) {
        Long rootA = a.rootTeamId();
        Long rootB = b.rootTeamId();
        return rootA != null && rootA.equals(rootB);
    }
}
