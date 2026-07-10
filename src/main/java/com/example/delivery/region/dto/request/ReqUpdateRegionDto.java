package com.example.delivery.region.dto.request;

import com.example.delivery.region.entity.RegionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "지역 수정 요청 DTO")
public class ReqUpdateRegionDto {
    @NotBlank(message = "지역명은 필수입니다.")
    @Size(max = 100, message = "지역명은 100자 이하로 입력해주세요.")
    @Schema(description = "지역명", example = "강남구")
    private String name;
    @Schema(description = "상위 지역 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID parentRegionId;
    @Schema(description = "지역 활성화 상태", example = "ACTIVE")
    private RegionStatus status;
}
