package com.example.delivery.region.entity;

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
 * 지역 분류. parent_region_id로 계층 구조(예: 서울 > 종로구 > 광화문)를 표현한다.
 * 다른 도메인과의 규칙과 동일하게 상위 Region은 객체 참조 대신 ID만 저장한다.
 * parent_region_id는 최상위 지역의 경우 없을 수 있어 nullable로 둔다.
 */
@Getter
@Entity
@Table(name = "p_region")
@AttributeOverride(name = "createdBy", column = @Column(name = "created_by", nullable = false, updatable = false))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "region_id")
    private UUID regionId;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "parent_region_id")
    private UUID parentRegionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RegionStatus status;

    public Region(String name, UUID parentRegionId) {
        this.name = name;
        this.parentRegionId = parentRegionId;
        this.status = RegionStatus.ACTIVE;
    }

    public Region(String name) {
        this.name = name;
        this.parentRegionId = null;
        this.status = RegionStatus.ACTIVE;
    }
}
