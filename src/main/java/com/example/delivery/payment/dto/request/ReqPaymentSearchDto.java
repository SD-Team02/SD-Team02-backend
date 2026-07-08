package com.example.delivery.payment.dto.request;

import com.example.delivery.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter // @ModelAttribute 조율용 바인딩 세터 유지
@NoArgsConstructor
public class ReqPaymentSearchDto {

    @Schema(description = "검색 시작 날짜", example = "2026-07-08")
    private LocalDate startDate;

    @Schema(description = "검색 종료 날짜", example = "2026-07-09")
    private LocalDate endDate;

    @Schema(description = "결제 상태 필터링", example = "SUCCESS")
    private PaymentStatus status;

}