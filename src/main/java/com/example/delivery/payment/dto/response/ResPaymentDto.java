package com.example.delivery.payment.dto.response;

import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResPaymentDto {

    private final UUID paymentId;
    private final UUID orderId;
    private final Integer amount;
    private final String paymentMethod;
    private final String cardCompany;
    private final PaymentStatus status;
    private final LocalDateTime approvedAt;

    public static ResPaymentDto from(Payment payment) {
        return new ResPaymentDto(
                payment.getPaymentId(),
                payment.getOrder().getOrderId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getCardCompany(),
                payment.getStatus(),
                payment.getApprovedAt()
        );
    }
}