package com.example.delivery.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

/**
 * 전체 API가 공통으로 사용하는 응답 포맷.
 * 컨트롤러는 항상 ResponseEntity<ApiResponse<T>>를 반환한다.
 *
 * 성공 예시:
 * { "success": true, "code": "OK", "message": "요청이 성공했습니다.", "data": {...} }
 *
 * 실패 예시 (GlobalExceptionHandler가 생성):
 * { "success": false, "code": "ORDER_NOT_FOUND", "message": "주문을 찾을 수 없습니다.", "data": null }
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", "요청이 성공했습니다.", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "OK", message, data);
    }

    public static ApiResponse<Void> successMessage(String message) {
        return new ApiResponse<>(true, "OK", message, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    public static <T> ApiResponse<T> error(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }
}