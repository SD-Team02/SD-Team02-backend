package com.example.delivery.payment.dto.response;

import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ResApprovePaymentDto {

    @Schema(description = "생성된 결제 고유 ID")
    private UUID paymentId;

    @Schema(description = "연동된 주문 고유 ID")
    private UUID orderId;

    @Schema(description = "최종 결제 상태")
    private PaymentStatus status;

    @Schema(description = "카드사 가상 승인 완료 시각")
    private LocalDateTime approvedAt;

    public ResApprovePaymentDto(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.orderId = payment.getOrder().getOrderId();
        this.status = payment.getStatus();
        this.approvedAt = payment.getApprovedAt();
    }
}