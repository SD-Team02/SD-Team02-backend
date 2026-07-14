package com.example.delivery.global.exception;

import lombok.Getter;

/**
 * 도메인 로직에서 의도적으로 던지는 예외의 최상위 타입.
 * 각 도메인은 필요하면 이 클래스를 상속한 구체 예외를 만들거나, 그대로 사용해도 된다.
 *
 * 사용 예:
 *   throw new BusinessException(ErrorCode.ORDER_CANCEL_TIME_EXCEEDED);
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
