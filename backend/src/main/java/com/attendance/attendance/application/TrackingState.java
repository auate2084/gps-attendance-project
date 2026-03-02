package com.attendance.attendance.application;

public enum TrackingState {
    // 반경 내 진입으로 자동 출근이 시작된 상태
    CHECKED_IN_STARTED,
    // 출근 반경 밖이라 출근이 시작되지 않은 상태
    OUTSIDE_CHECKIN_RADIUS,
    // 출근 상태를 그대로 유지하는 상태
    STILL_CHECKED_IN,
    // 퇴근 반경 이탈이 감지되어 타이머가 시작된 상태
    OUTSIDE_CHECKOUT_RADIUS_TIMER_STARTED,
    // 퇴근 반경 밖에 있지만 유예 시간 내인 상태
    OUTSIDE_CHECKOUT_RADIUS,
    // 다시 퇴근 반경 안으로 들어온 상태
    BACK_INSIDE_CHECKOUT_RADIUS,
    // 유예 시간 초과로 자동 퇴근 처리된 상태
    CHECKED_OUT_AUTOMATIC
}
