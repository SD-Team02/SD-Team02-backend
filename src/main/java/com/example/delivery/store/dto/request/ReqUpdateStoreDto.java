package com.example.delivery.store.dto.request;

import com.example.delivery.store.entity.StoreStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "가게 수정 요청 DTO")
public class ReqUpdateStoreDto {
    @Schema(description = "카테고리 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID categoryId;
    @Schema(description = "카테고리 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID regionId;
    @Schema(description = "가게명", example = "엽기떡볶이 문정점")
    private String name;
    @Schema(description = "주소", example = "서울 송파구 문정동 620")
    private String address;
    @Schema(description = "주소", example = "서울 송파구 문정동 620")
    private String phone;
    @Schema(description = "오픈시간", example = "09:00")
    private LocalTime openTime;
    @Schema(description = "마감시간", example = "09:00")
    private LocalTime closeTime;
    @Schema(description = "영업 상태", example = "OPEN")
    private StoreStatus status;
}
