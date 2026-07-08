package com.example.delivery.payment.dto.response;

import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResApprovePaymentDto {

    @JsonProperty("payment_id") // 노션 명세서 양식의 snake_case 최종 반영 요구사항 충족
    private final UUID paymentId;
    private final UUID orderId;
    private final PaymentStatus status;
    private final LocalDateTime approvedAt;

    public static ResApprovePaymentDto from(Payment payment) {
        return new ResApprovePaymentDto(
                payment.getPaymentId(),
                payment.getOrder().getOrderId(),
                payment.getStatus(),
                payment.getApprovedAt()
        );
    }
}