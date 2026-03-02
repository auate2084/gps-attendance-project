package com.attendance.user.infrastructure;

import com.attendance.user.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    Optional<User> findByLoginId(String loginId);

    @EntityGraph(attributePaths = {"team", "workPolicy"})
    @Query("SELECT u FROM User u WHERE u.loginId = :loginId")
    Optional<User> findByLoginIdWithRelations(@Param("loginId") String loginId);

    @EntityGraph(attributePaths = {"team", "workPolicy"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithRelations(@Param("id") Long id);

    List<User> findByTeamIdIsNotNull();

    List<User> findByTeamId(Long teamId);

    List<User> findByTeamIdIn(List<Long> teamIds);
}
