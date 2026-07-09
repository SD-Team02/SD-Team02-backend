package com.example.delivery.category.entity;

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

/**
 * 음식점 카테고리(한식/중식/분식/치킨/피자 등). 추가/수정이 가능해야 하는 요구사항이라
 * 코드에 하드코딩된 enum이 아니라 테이블로 관리한다.
 */
@Getter
@Entity
@Table(name = "p_category")
@AttributeOverride(name = "createdBy", column = @Column(name = "created_by", nullable = false, updatable = false))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CategoryStatus status;

    public Category(String name) {
        this.name = name;
        this.status = CategoryStatus.ACTIVE;
    }

    //카테고리 수정 메서드
    public void update(String name, CategoryStatus status) {
        this.name = name;
        this.status = status;
    }

    //카테고리명 변경 메서드
    public void rename(String name) {
        this.name = name;
    }
}
