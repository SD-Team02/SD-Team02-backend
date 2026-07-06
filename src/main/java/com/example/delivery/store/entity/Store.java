package com.example.delivery.store.entity;

import java.time.LocalTime;
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
@Table(name = "p_store")
@AttributeOverride(name = "createdBy", column = @Column(name = "created_by", nullable = false, updatable = false))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "region_id", nullable = false)
    private UUID regionId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phone", length = 30, nullable = false)
    private String phone;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StoreStatus status;

    public Store(Long userId, UUID categoryId, UUID regionId, String name, String address,
                 String phone, LocalTime openTime, LocalTime closeTime) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.regionId = regionId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.status = StoreStatus.OPEN;
    }

    public void changeInfo(String name, String address, String phone, LocalTime openTime, LocalTime closeTime) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public void changeCategory(UUID categoryId) {
        this.categoryId = categoryId;
    }
}
