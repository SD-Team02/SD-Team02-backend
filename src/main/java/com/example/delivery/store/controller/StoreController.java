package com.example.delivery.store.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.region.dto.response.ResGetRegionDto;
import com.example.delivery.store.dto.request.ReqCreateStoreDto;
import com.example.delivery.store.dto.request.ReqUpdateStoreDto;
import com.example.delivery.store.dto.response.ResCreateStoreDto;
import com.example.delivery.store.dto.response.ResDeleteStoreDto;
import com.example.delivery.store.dto.response.ResGetStoreDto;
import com.example.delivery.store.entity.StoreStatus;
import com.example.delivery.store.service.StoreService;
import com.example.delivery.user.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Tag(name = "가게", description = "가게 API")
public class StoreController {
    private final StoreService storeService;;

    @Operation(summary = "가게 등록")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "가게 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "지역을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 가게입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 요청에 대한 권한이 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PreAuthorize("hasAnyAuthority('OWNER','MANAGER', 'MASTER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ResCreateStoreDto>> createStore(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ReqCreateStoreDto reqCreateStoreDto){
        ResCreateStoreDto resCreateStoreDto = storeService.createStore(userDetails.getUser().getUserId(), reqCreateStoreDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("가게 등록 성공",resCreateStoreDto));
    }

    @Operation(summary = "전체 가게 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "전체 가게 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "정렬 기준이 올바르지 않습니다."),
    })
    @PreAuthorize("hasAnyAuthority('CUSTOMER','OWNER','MANAGER', 'MASTER')")
    @GetMapping
    ResponseEntity<ApiResponse<PageResponse<?>>> getAllStores(
            @RequestParam(defaultValue = "OPEN")StoreStatus status,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable){

        Page<ResGetStoreDto> resStores = storeService.getAllStores(status,pageable);

        return ResponseEntity.ok(ApiResponse.success("전체 가게 조회 성공", PageResponse.from(resStores)));
    }

    @Operation(summary = "가게 상세 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "가게 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 타입이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가게를 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PreAuthorize("hasAnyAuthority('CUSTOMER','OWNER','MANAGER', 'MASTER')")
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<ResGetStoreDto>> getStore(@PathVariable UUID storeId){
        ResGetStoreDto resGetStoreDto = storeService.getStore(storeId);
        return ResponseEntity.ok(ApiResponse.success("가게 상세 조회 성공",resGetStoreDto));
    }

    @Operation(summary = "가게 수정")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "가게 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문을 읽을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가게를 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "지역을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 요청에 대한 권한이 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PreAuthorize("hasAnyAuthority('OWNER','MANAGER', 'MASTER')")
    @PutMapping("/{storeId}")
    public ResponseEntity<ApiResponse<ResGetStoreDto>> updateStore(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID storeId,
            @Valid @RequestBody ReqUpdateStoreDto reqUpdateStoreDto){
        Long userId = userDetails.getUser().getUserId();
        ResGetStoreDto resGetStoreDto = storeService.updateStore(reqUpdateStoreDto, storeId, userId);
        return ResponseEntity.ok(ApiResponse.success("가게 수정 성공",resGetStoreDto));
    }

    @Operation(summary = "가게 삭제")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "가게 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 삭제된 가게입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 요청에 대한 권한이 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PreAuthorize("hasAnyAuthority('OWNER','MANAGER', 'MASTER')")
    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<ResDeleteStoreDto>> deleteStore(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID storeId){
        Long userId = userDetails.getUser().getUserId();
        ResDeleteStoreDto resDeleteStoreDto = storeService.deleteStore(storeId,userId);
        return ResponseEntity.ok(ApiResponse.success("가게 삭제 성공",resDeleteStoreDto));
    }
}
