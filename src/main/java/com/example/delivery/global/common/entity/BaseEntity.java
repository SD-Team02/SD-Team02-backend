package com.example.delivery.global.common.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * 모든 도메인 Entity가 상속하는 공통 audit 필드.
 * - 생성/수정/삭제 시각 및 수행자를 기록한다.
 * - 실제 delete()가 아닌 Soft Delete(deletedAt/deletedBy) 방식을 사용한다.
 *
 * 수정/삭제가 발생하지 않는 테이블(예: AI 요청 로그 등 append-only 테이블)은
 * 이 클래스를 상속하지 말고 필요한 필드(createdAt, createdBy)만 엔티티에 직접 선언한다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    /**
     * Soft Delete 처리. 실제 row는 삭제하지 않는다.
     * 예) orderRepository.findById(id).ifPresent(o -> o.softDelete(currentUserId));
     */
    public void softDelete(Long deletedByUserId) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedByUserId;
    }

    /** 삭제 취소(복구)가 필요한 경우 사용. */
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}