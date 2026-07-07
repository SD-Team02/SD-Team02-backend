package com.example.delivery.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "카테고리 생성 요청 DTO")
public class ReqCreateCategoryDto {

    @NotBlank(message = "카테고리명은 필수입니다.")
    @Schema(description = "카테고리명", example = "한식")
    private String name;
}
