package com.example.delivery.payment.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.delivery.global.common.entity.BaseEntity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_payment")
@AttributeOverride(name = "createdBy", column = @Column(name = "created_by", nullable = false, updatable = false))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "payment_method", length = 30, nullable = false)
    private String paymentMethod;

    @Column(name = "card_company", length = 50, nullable = false)
    private String cardCompany;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public Payment(UUID orderId, String paymentMethod, String cardCompany, Integer amount) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.cardCompany = cardCompany;
        this.amount = amount;
        this.status = PaymentStatus.READY;
    }

    public void approve() {
        this.status = PaymentStatus.SUCCESS;
        this.approvedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = PaymentStatus.FAIL;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
    }
}
