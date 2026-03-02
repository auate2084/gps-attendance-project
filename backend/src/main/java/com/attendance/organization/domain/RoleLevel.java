package com.attendance.organization.domain;

public enum RoleLevel {
    DEPARTMENT_HEAD(4, VisibilityScope.TEAM_AND_DESCENDANTS),
    TEAM_LEAD(3, VisibilityScope.TEAM_AND_DESCENDANTS),
    MANAGER(2, VisibilityScope.TEAM_AND_DESCENDANTS),
    TEAM_MEMBER(1, VisibilityScope.SAME_TEAM);

    private final int rank;
    private final VisibilityScope visibilityScope;

    RoleLevel(int rank, VisibilityScope visibilityScope) {
        this.rank = rank;
        this.visibilityScope = visibilityScope;
    }

    public boolean isHigherThan(RoleLevel other) {
        return this.rank > other.rank;
    }

    public VisibilityScope visibilityScope() {
        return visibilityScope;
    }
}
