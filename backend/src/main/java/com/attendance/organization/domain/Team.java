package com.attendance.organization.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "teams",
        uniqueConstraints = @UniqueConstraint(columnNames = {"parent_team_id", "name"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // 팀 식별자(PK)
    private Long id;

    @Column(nullable = false)
    // 팀 이름
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_team_id")
    // 상위 팀(FK), 루트 팀이면 null
    private Team parentTeam;

    // 팀 이름과 상위 팀 정보를 받아 팀 엔티티를 생성한다.
    public Team(String name, Team parentTeam) {
        this.name = name;
        this.parentTeam = parentTeam;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeParentTeam(Team parentTeam) {
        this.parentTeam = parentTeam;
    }

    private static final int MAX_HIERARCHY_DEPTH = 50;

    // 현재 팀에서 부모 체인을 따라가 최상위 루트 팀 ID를 반환한다.
    public Long rootTeamId() {
        Team current = this;
        int depth = 0;
        while (current.parentTeam != null) {
            if (++depth > MAX_HIERARCHY_DEPTH) {
                throw new IllegalStateException("team hierarchy depth exceeds maximum (" + MAX_HIERARCHY_DEPTH + "), possible cycle detected");
            }
            current = current.parentTeam;
        }
        return current.id;
    }
}
