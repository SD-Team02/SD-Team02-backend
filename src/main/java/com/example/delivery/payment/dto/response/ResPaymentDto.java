package com.example.delivery.payment.dto.response;

import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ResPaymentDto {

    @Schema(description = "결제 고유 ID")
    private UUID paymentId;

    @Schema(description = "주문 고유 ID")
    private UUID orderId;

    @Schema(description = "결제 금액")
    private Integer amount;

    @Schema(description = "결제 수단")
    private String paymentMethod;

    @Schema(description = "카드 회사명")
    private String cardCompany;

    @Schema(description = "결제 진행 상태")
    private PaymentStatus status;

    @Schema(description = "결제 승인 일시")
    private LocalDateTime approvedAt;

    public ResPaymentDto(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.orderId = payment.getOrder().getOrderId();
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.cardCompany = payment.getCardCompany();
        this.status = payment.getStatus();
        this.approvedAt = payment.getApprovedAt();
    }
}