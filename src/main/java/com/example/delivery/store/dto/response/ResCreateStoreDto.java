package com.example.delivery.store.dto.response;

import com.example.delivery.store.entity.Store;
import com.example.delivery.store.entity.StoreStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "가게 등록 응답 DTO")
public class ResCreateStoreDto {
    @Schema(description = "가게 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID storeId;
    @Schema(description = "유저 id", example = "1")
    private Long userId;
    @Schema(description = "카테고리 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID categoryId;
    @Schema(description = "카테고리명", example = "한식")
    private String categoryName;
    @Schema(description = "지역 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID regionId;
    @Schema(description = "지역명", example = "강남구")
    private String regionName;
    @Schema(description = "가게명", example = "엽기떡볶이 문정점")
    private String name;
    @Schema(description = "주소", example = "서울 송파구 문정동 620")
    private String address;
    @Schema(description = "전화번호", example = "01011112222")
    private String phone;
    @Schema(description = "오픈시간", example = "09:00")
    private LocalTime openTime;
    @Schema(description = "마감시간", example = "09:00")
    private LocalTime closeTime;
    @Schema(description = "영업 상태", example = "OPEN")
    private StoreStatus status;
    @Schema(description = "등록 시간", example = "2026-07-07T12:34:56.123456700")
    private LocalDateTime createdAt;
    @Schema(description = "등록한 유저 id", example = "1")
    private Long createdBy;

    public static ResCreateStoreDto from(
            Store store, String categoryName, String regionName) {
        return ResCreateStoreDto.builder()
                .storeId(store.getStoreId())
                .userId(store.getUserId())
                .categoryId(store.getCategoryId())
                .categoryName(categoryName)
                .regionId(store.getRegionId())
                .regionName(regionName)
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .status(store.getStatus())
                .createdAt(store.getCreatedAt())
                .createdBy(store.getCreatedBy())
                .build();
    }
}
