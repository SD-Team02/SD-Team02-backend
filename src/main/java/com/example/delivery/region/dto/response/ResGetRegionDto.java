package com.example.delivery.region.dto.response;

import com.example.delivery.region.entity.Region;
import com.example.delivery.region.entity.RegionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "지역 조회 응답 DTO")
public class ResGetRegionDto {
    @Schema(description = "지역 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID regionId;
    @Schema(description = "지역명", example = "강남구")
    private String name;
    @Schema(description = "상위 지역 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID parentRegionId;
    @Schema(description = "상위 지역명", example = "서울시")
    private String parentRegionName;
    @Schema(description = "주문 가능 여부", example = "ACTIVE")
    private RegionStatus status;
    @Schema(description = "지역 등록 날짜", example = "2026-07-07T12:34:56.123456700")
    private LocalDateTime createdAt;
    @Schema(description = "지역 수정 날짜", example = "2026-07-07T12:34:56.123456700")
    private LocalDateTime updatedAt;

    public static ResGetRegionDto from(Region region, String parentRegionName){
        return ResGetRegionDto.builder()
                .regionId(region.getRegionId())
                .name(region.getName())
                .parentRegionId(region.getParentRegionId())
                .parentRegionName(parentRegionName)
                .status(region.getStatus())
                .createdAt(region.getCreatedAt())
                .updatedAt(region.getUpdatedAt())
                .build();
    }
}
