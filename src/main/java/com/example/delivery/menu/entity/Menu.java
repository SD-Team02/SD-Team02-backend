package com.example.delivery.menu.entity;

import java.util.UUID;

import com.example.delivery.global.common.entity.BaseEntity;

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
@Table(name = "p_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "menu_id")
    private UUID menuId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "menu_name", length = 100, nullable = false)
    private String menuName;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ai_generated")
    private Boolean aiGenerated;

    @Enumerated(EnumType.STRING)
    @Column(name = "menu_status")
    private MenuStatus menuStatus;

    public Menu(UUID storeId, String menuName, Integer price, String description, Boolean aiGenerated) {
        this.storeId = storeId;
        this.menuName = menuName;
        this.price = price;
        this.description = description;
        this.aiGenerated = aiGenerated;
        this.menuStatus = MenuStatus.NORMAL;
    }

    public void changeDescription(String description, boolean aiGenerated) {
        this.description = description;
        this.aiGenerated = aiGenerated;
    }

    public void changePrice(Integer price) {
        this.price = price;
    }

    /** 상품 숨김. soft delete(삭제)와는 별개의 상태 전환이다. */
    public void hide() {
        this.menuStatus = MenuStatus.HIDDEN;
    }

    public void show() {
        this.menuStatus = MenuStatus.NORMAL;
    }
}
