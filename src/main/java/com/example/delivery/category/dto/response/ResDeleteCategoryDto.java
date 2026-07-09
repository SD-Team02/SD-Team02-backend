package com.example.delivery.category.dto.response;

import com.example.delivery.category.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ResDeleteCategoryDto {
    @Schema(description = "카테고리 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID categoryId;
    @Schema(description = "카테고리명", example = "한식")
    private String name;

    public static ResDeleteCategoryDto from(Category category) {
        return ResDeleteCategoryDto.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .build();
    }
}
