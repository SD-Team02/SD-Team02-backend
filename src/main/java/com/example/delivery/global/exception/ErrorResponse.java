package com.example.delivery.global.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import lombok.Getter;

/**
 * Validation 실패시 필드별 상세 오류를 포함하기 위한 응답 payload.
 * ApiResponse.data 자리에 담겨서 내려간다.
 * { "success": false, "code": "COMMON_400_1", "message": "...", "data": { "errors": [...] } }
 */
@Getter
public class ErrorResponse {

    private final List<FieldErrorDetail> errors;

    private ErrorResponse(List<FieldErrorDetail> errors) {
        this.errors = errors;
    }

    public static ErrorResponse of(BindingResult bindingResult) {
        List<FieldErrorDetail> details = new ArrayList<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            details.add(new FieldErrorDetail(
                    fieldError.getField(),
                    fieldError.getRejectedValue() == null ? "" : fieldError.getRejectedValue().toString(),
                    fieldError.getDefaultMessage()
            ));
        }
        return new ErrorResponse(details);
    }

    @Getter
    public static class FieldErrorDetail {
        private final String field;
        private final String rejectedValue;
        private final String reason;

        public FieldErrorDetail(String field, String rejectedValue, String reason) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }
    }
}
