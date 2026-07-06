package com.example.delivery.payment.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.delivery.global.common.entity.BaseEntity;

import com.example.delivery.order.entity.Order;
import jakarta.persistence.*;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true) // FK 지정 및 1:1 보장을 위한 유니크 제약
    private Order order;

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

    public Payment(Order order, String paymentMethod, String cardCompany, Integer amount) {
        this.order = order;
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
