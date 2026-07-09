package com.example.delivery.category.dto.request;

import com.example.delivery.category.entity.CategoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "카테고리 수정 요청 DTO")
public class ReqUpdateCategoryDto {
    @NotBlank(message = "카테고리명은 필수입니다.")
    @Size(max = 100, message = "카테고리명은 100자 이하로 입력해주세요.")
    @Schema(description = "카테고리명", example = "한식")
    private String name;

    @NotNull(message = "카테고리 상태는 필수입니다.")
    @Schema(description = "카테고리 상태(사용 여부)", example = "ACTIVE")
    private CategoryStatus status;
}
