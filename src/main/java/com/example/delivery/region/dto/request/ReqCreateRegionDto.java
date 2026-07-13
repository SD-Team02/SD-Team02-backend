package com.example.delivery.region.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@Schema(description = "지역 등록 요청 DTO")
public class ReqCreateRegionDto {
    @NotBlank(message = "지역명은 필수입니다.")
    @Size(max = 100, message = "지역명은 100자 이하로 입력해주세요.")
    @Schema(description = "지역명", example = "강남구")
    private String name;
    @Schema(description = "상위 지역 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID parentRegionId;

    public ReqCreateRegionDto(String name, UUID parentRegionId) {
        this.name = name;
        this.parentRegionId = parentRegionId;
    }
}
