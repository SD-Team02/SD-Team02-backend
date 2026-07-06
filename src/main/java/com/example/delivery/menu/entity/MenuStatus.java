package com.example.delivery.menu.entity;

/**
 * 상품(메뉴) 노출 상태. 숨김(HIDDEN)과 삭제는 서로 다른 필드로 관리한다 (요구사항 필수 조건) —
 * HIDDEN 여부는 BaseEntity.deletedAt과 무관하게 별도로 판단한다.
 */
public enum MenuStatus {
    NORMAL, HIDDEN
}
