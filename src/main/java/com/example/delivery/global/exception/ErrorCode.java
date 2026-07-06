package com.example.delivery.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 전역 에러 코드 정의.
 *
 * 규칙: 새 에러가 필요하면 이 파일에 항목을 추가한다 (담당 도메인 접두어 사용 권장).
 * 예) 주문 담당 -> ORDER_XXX, 결제 담당 -> PAYMENT_XXX, 가게 담당 -> STORE_XXX
 * PR로 추가하고 공통/인프라 담당자에게 리뷰 요청.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common (400/401/403/404/405/500)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_400_1", "입력값이 올바르지 않습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "COMMON_400_2", "요청 타입이 올바르지 않습니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON_400_3", "필수 요청 파라미터가 누락되었습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401_1", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "COMMON_401_2", "토큰이 유효하지 않거나 만료되었습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON_403_1", "해당 요청에 대한 권한이 없습니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404_1", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405_1", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500_1", "서버 내부 오류가 발생했습니다."),

    // User (인증 담당 참고용 예시)
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "USER_409_1", "이미 사용중인 아이디입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER_409_2", "이미 사용중인 닉네임입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_1", "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER_400_1", "비밀번호 형식이 올바르지 않습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "USER_401_1", "아이디 또는 비밀번호가 일치하지 않습니다."),

    // Store (가게 담당 참고용 예시)
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE_404_1", "가게를 찾을 수 없습니다."),
    STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "STORE_403_1", "본인 소유의 가게가 아닙니다."),

    // Order (주문 담당 참고용 예시)
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_404_1", "주문을 찾을 수 없습니다."),
    ORDER_CANCEL_TIME_EXCEEDED(HttpStatus.BAD_REQUEST, "ORDER_400_1", "주문 취소 가능 시간(5분)이 지났습니다."),
    ORDER_STATUS_TRANSITION_INVALID(HttpStatus.BAD_REQUEST, "ORDER_400_2", "해당 상태로 변경할 수 없습니다."),

    // Payment (결제 담당 참고용 예시)
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404_1", "결제 내역을 찾을 수 없습니다."),

    // Review (주문/리뷰 담당 참고용 예시)
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW_409_1", "이미 작성한 리뷰가 있습니다."),
    REVIEW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "REVIEW_400_1", "배송 완료된 주문에만 리뷰를 작성할 수 있습니다."),

    // AI (메뉴+AI 담당 참고용 예시)
    AI_REQUEST_TEXT_TOO_LONG(HttpStatus.BAD_REQUEST, "AI_400_1", "입력 텍스트가 글자수 제한을 초과했습니다."),
    AI_API_CALL_FAILED(HttpStatus.BAD_GATEWAY, "AI_502_1", "AI API 호출에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}