package com.example.delivery.payment.dto.request;

import com.example.delivery.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter // @ModelAttribute 조율용 바인딩 세터 유지
@NoArgsConstructor
public class ReqPaymentSearchDto {

    @NotNull(message = "검색 시작 날짜는 필수 입력 사항입니다.")
    @Schema(description = "검색 시작 날짜", example = "2026-07-12")
    private LocalDate startDate;

    @NotNull(message = "검색 종료 날짜는 필수 입력 사항입니다.")
    @Schema(description = "검색 종료 날짜", example = "2026-07-13")
    private LocalDate endDate;

    @Schema(description = "결제 상태 필터링", example = "SUCCESS")
    private PaymentStatus status;

}