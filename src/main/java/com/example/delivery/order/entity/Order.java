package com.example.delivery.order.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.delivery.global.common.entity.BaseEntity;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;

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
@Table(name = "p_order")
@AttributeOverride(name = "createdBy", column = @Column(name = "created_by", nullable = false, updatable = false))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    private static final long CANCELABLE_MINUTES = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    public Order(Long userId, UUID storeId, String address, String detailAddress, Integer totalPrice) {
        this.userId = userId;
        this.storeId = storeId;
        this.address = address;
        this.detailAddress = detailAddress;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.REQUESTED;
    }

    public void changeStatus(OrderStatus status) {
        validateStatusTransition(status);
        this.status = status;
    }

    private void validateStatusTransition(OrderStatus nextStatus) {
        if (!this.status.canChangeTo(nextStatus)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_TRANSITION_INVALID);
        }
    }

    /** 주문 생성 후 5분 이내에만 취소 가능 (요구사항 필수 조건). */
    public void cancel() {
        validateStatusTransition(OrderStatus.CANCELED);

        if (Duration.between(getCreatedAt(), LocalDateTime.now()).toMinutes() >= CANCELABLE_MINUTES) {
            throw new BusinessException(ErrorCode.ORDER_CANCEL_TIME_EXCEEDED);
        }
        this.status = OrderStatus.CANCELED;
    }
}
