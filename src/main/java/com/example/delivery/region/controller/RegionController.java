package com.example.delivery.region.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.region.dto.request.ReqCreateRegionDto;
import com.example.delivery.region.dto.response.ResCreateRegionDto;
import com.example.delivery.region.entity.RegionStatus;
import com.example.delivery.region.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
@Tag(name = "지역", description = "지역 API")
public class RegionController {
    private final RegionService regionService;

    @Operation(summary = "지역 생성")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "지역 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 지역입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상위 지역을 찾을 수 없습니다."),
//            #TO-DO : 모든 인증이 필요한 API에 공통으로 적용되는 어노테이션을 별도로 생성하거나, 전역 필터 단에서 처리되도록 구조화하는 방식을 고려
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ResCreateRegionDto>> createRegion(@Valid @RequestBody ReqCreateRegionDto reqCreateRegionDto){
        ResCreateRegionDto resCreateRegionDto = regionService.createRegion(reqCreateRegionDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("지역 생성 성공",resCreateRegionDto));
    }


}
