package com.attendance.attendance.domain;

import com.attendance.user.domain.User;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // 근무 세션 식별자(PK)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    // 이 세션의 소유 사용자(FK)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id")
    // 이 세션에 적용된 근무 정책(FK)
    private WorkPolicy workPolicy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // 현재 근무 상태(출근/퇴근)
    private WorkSessionStatus status;

    @Column(nullable = false)
    // 출근 처리 시각
    private LocalDateTime checkInAt;

    @Column
    // 퇴근 처리 시각
    private LocalDateTime checkOutAt;

    @Column(nullable = false)
    // 출근 처리 당시 위도
    private double checkInLatitude;

    @Column(nullable = false)
    // 출근 처리 당시 경도
    private double checkInLongitude;

    @Column
    // 퇴근 처리 당시 위도
    private Double checkOutLatitude;

    @Column
    // 퇴근 처리 당시 경도
    private Double checkOutLongitude;

    @Column
    // 퇴근 반경 밖으로 벗어나기 시작한 시각
    private LocalDateTime outsideSince;

    @Column
    // 마지막으로 수신한 위치의 위도
    private Double lastLatitude;

    @Column
    // 마지막으로 수신한 위치의 경도
    private Double lastLongitude;

    // 출근 시작 시점의 정보를 기반으로 근무 세션을 생성한다.
    public WorkSession(User user, WorkPolicy workPolicy, LocalDateTime checkInAt, double checkInLatitude, double checkInLongitude) {
        this.user = user;
        this.workPolicy = workPolicy;
        this.status = WorkSessionStatus.CHECKED_IN;
        this.checkInAt = checkInAt;
        this.checkInLatitude = checkInLatitude;
        this.checkInLongitude = checkInLongitude;
        this.lastLatitude = checkInLatitude;
        this.lastLongitude = checkInLongitude;
    }

    // 퇴근 반경 밖으로 처음 벗어난 시각을 기록한다.
    public void markOutside(LocalDateTime observedAt) {
        if (outsideSince == null) {
            this.outsideSince = observedAt;
        }
    }

    // 반경 이탈 상태를 해제한다.
    public void clearOutsideState() {
        this.outsideSince = null;
    }

    // 마지막으로 관측된 위치 좌표를 갱신한다.
    public void updateLastLocation(double latitude, double longitude) {
        this.lastLatitude = latitude;
        this.lastLongitude = longitude;
    }

    // 세션을 퇴근 상태로 전환하고 퇴근 위치/시각을 기록한다.
    public void checkout(LocalDateTime observedAt, double latitude, double longitude) {
        this.status = WorkSessionStatus.CHECKED_OUT;
        this.checkOutAt = observedAt;
        this.checkOutLatitude = latitude;
        this.checkOutLongitude = longitude;
        this.outsideSince = null;
    }
}
