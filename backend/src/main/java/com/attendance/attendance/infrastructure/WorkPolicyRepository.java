package com.attendance.attendance.infrastructure;

import com.attendance.attendance.domain.WorkPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkPolicyRepository extends JpaRepository<WorkPolicy, Long> {
    Optional<WorkPolicy> findFirstByTeamIdOrderByIdDesc(Long teamId);
}
