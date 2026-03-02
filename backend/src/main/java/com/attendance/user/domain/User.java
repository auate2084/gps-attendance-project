package com.attendance.user.domain;

import com.attendance.attendance.domain.WorkPolicy;
import com.attendance.organization.domain.RoleLevel;
import com.attendance.organization.domain.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleLevel roleLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private WorkPolicy workPolicy;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean hrAuthority;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime lastLoginAt;

    public User(
            String loginId,
            String passwordHash,
            String email,
            String name,
            RoleLevel roleLevel,
            Team team,
            WorkPolicy workPolicy
    ) {
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.email = email;
        this.name = name;
        this.roleLevel = roleLevel;
        this.team = team;
        this.workPolicy = workPolicy;
        this.active = true;
        this.hrAuthority = false;
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void markLoginSuccess(LocalDateTime when) {
        this.lastLoginAt = when;
    }

    public void changeRoleLevel(RoleLevel roleLevel) {
        this.roleLevel = roleLevel;
    }

    public void changeTeam(Team team) {
        this.team = team;
    }

    public void assignWorkPolicy(WorkPolicy workPolicy) {
        this.workPolicy = workPolicy;
    }

    public void grantHrAuthority() {
        this.hrAuthority = true;
    }

    public void revokeHrAuthority() {
        this.hrAuthority = false;
    }
}
