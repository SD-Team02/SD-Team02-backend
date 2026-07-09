package com.example.delivery.region.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.region.dto.request.ReqCreateRegionDto;
import com.example.delivery.region.dto.response.ResCreateRegionDto;
import com.example.delivery.region.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
@Tag(name = "지역", description = "지역 API")
public class RegionController {
    private final RegionService regionService;

    @Operation(summary = "지역 생성")
    @ApiResponses({

    })
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createRegion(@Valid @RequestBody ReqCreateRegionDto reqCreateRegionDto){
        ResCreateRegionDto resCreateRegionDto = regionService.createRegion(reqCreateRegionDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("지역 생성 성공",resCreateRegionDto));
    }
}
