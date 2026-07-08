package com.example.delivery.payment.dto.response;

import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ResPaymentDto {

    private UUID paymentId;
    private UUID orderId;
    private Integer amount;
    private String paymentMethod;
    private String cardCompany;
    private PaymentStatus status;
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