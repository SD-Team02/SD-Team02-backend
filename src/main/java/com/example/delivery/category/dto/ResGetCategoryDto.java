package com.example.delivery.category.dto;

import com.example.delivery.category.entity.Category;
import com.example.delivery.category.entity.CategoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "카테고리 조회 응답 DTO")
public class ResGetCategoryDto {
    @Schema(description = "카테고리 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID categoryId;
    @Schema(description = "카테고리명", example = "한식")
    private String name;
    @Schema(description = "카테고리 사용 여부", example = "ACTIVE")
    private CategoryStatus status;
    @Schema(description = "카테고리 생성 날짜", example = "2026-07-07T12:34:56.123456700")
    private LocalDateTime createdAt;
    @Schema(description = "카테고리 수정 날짜", example = "2026-07-07T12:34:56.123456700")
    private LocalDateTime updatedAt;

    public static ResGetCategoryDto from(Category category) {
        return ResGetCategoryDto.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .status(category.getStatus())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
