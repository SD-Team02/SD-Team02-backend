package com.example.delivery.region.dto.response;

import com.example.delivery.region.entity.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ResDeleteRegionDto {
    @Schema(description = "지역 id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID regionId;
    @Schema(description = "지역명", example = "강남구")
    private String name;

    public static ResDeleteRegionDto from(Region region){
        return ResDeleteRegionDto.builder()
                .regionId(region.getRegionId())
                .name(region.getName())
                .build();
    }
}
