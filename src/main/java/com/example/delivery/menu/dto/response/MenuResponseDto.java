package com.example.delivery.menu.dto.response;

import com.example.delivery.menu.entity.Menu;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class MenuResponseDto {
    private UUID menuId;                // 메뉴 아이디
    private UUID storeId;               // 가게 아이디
    private String menuName;            // 가게 이름
    private Integer price;              // 메뉴 가격
    private String description;         // 메뉴 설명
    private Boolean aiGenerated;        // AI 설명 생성 여부
    private Enum menuStatus;            // 메뉴 상태값 (정상: NORMAL,숨김: HIDDEN)
    private LocalDateTime createdAt;    // 생성날짜
    private Long createdBy;             // 생성자
    private LocalDateTime updatedAt;    // 수정날짜
    private Long updatedBy;             // 수정자
    private LocalDateTime deletedAt;    // 삭제날짜
    private Long deletedBy;             // 삭제자

    public MenuResponseDto(Menu menu) {
        this.menuId = menu.getMenuId();
        this.storeId = menu.getMenuId();
        this.menuName = menu.getMenuName();
        this.price = menu.getPrice();
        this.description = menu.getDescription();
        this.aiGenerated = menu.getAiGenerated();
        this.menuStatus = menu.getMenuStatus();
        this.createdAt = menu.getCreatedAt();
        this.createdBy = menu.getCreatedBy();
        this.updatedAt = menu.getUpdatedAt();
        this.updatedBy = menu.getUpdatedBy();
        this.deletedAt = menu.getDeletedAt();
        this.deletedBy = menu.getDeletedBy();
    }

    public static MenuResponseDto from(Menu menu) {
        return new MenuResponseDto(menu);
    }
}
