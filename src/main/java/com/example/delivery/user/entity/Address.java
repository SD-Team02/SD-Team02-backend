package com.example.delivery.user.entity;

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

@Getter
@Entity
@Table(name = "p_address")
@AttributeOverride(name = "createdBy", column = @Column(name = "created_by", nullable = false, updatable = false))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "address_id")
    private UUID addressId;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    public Address(Long userId, String address, String detailAddress) {
        this.userId = userId;
        this.address = address;
        this.detailAddress = detailAddress;
    }

    public void update(String address, String detailAddress) {
        this.address = address;
        this.detailAddress = detailAddress;
    }
}
