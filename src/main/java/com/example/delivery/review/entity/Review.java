package com.example.delivery.review.entity;

import java.util.UUID;

import com.example.delivery.global.common.entity.BaseEntity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

/** rating은 요구사항상 1~5 범위만 허용 (DB CHECK 제약 + 요청 DTO의 Bean Validation으로 이중 검증). */
@Getter
@Entity
@Table(name = "p_review")
@Check(constraints = "rating between 1 and 5")
@AttributeOverride(name = "createdBy", column = @Column(name = "created_by", nullable = false, updatable = false))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id")
    private UUID reviewId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    public Review(UUID storeId, UUID orderId, Long userId, Integer rating, String content) {
        this.storeId = storeId;
        this.orderId = orderId;
        this.userId = userId;
        this.rating = rating;
        this.content = content;
    }
}
