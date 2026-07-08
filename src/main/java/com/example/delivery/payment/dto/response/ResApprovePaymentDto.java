package com.example.delivery.payment.dto.response;

import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ResApprovePaymentDto {

    private UUID paymentId;
    private UUID orderId;
    private PaymentStatus status;
    private LocalDateTime approvedAt;

    public ResApprovePaymentDto(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.orderId = payment.getOrder().getOrderId();
        this.status = payment.getStatus();
        this.approvedAt = payment.getApprovedAt();
    }
}