package com.attendance.shared.exception;

public class BusinessException extends RuntimeException {
    // 비즈니스 규칙 위반 메시지를 담는 예외를 생성한다.
    public BusinessException(String message) {
        super(message);
    }
}
