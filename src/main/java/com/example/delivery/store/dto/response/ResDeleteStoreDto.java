package com.example.delivery.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(description = "가게 삭제 응답 DTO")
public class ResDeleteStoreDto {
    @Schema(description = "가게 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID storeId;

    @Schema(description = "가게명", example = "엽기떡볶이 문정점")
    private String name;

    public static ResDeleteStoreDto from(UUID storeId, String name){
        return ResDeleteStoreDto.builder()
                .storeId(storeId)
                .name(name)
                .build();
    }
}
