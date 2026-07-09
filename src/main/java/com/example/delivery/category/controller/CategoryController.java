package com.example.delivery.category.controller;

import com.example.delivery.category.dto.ReqCreateCategoryDto;
import com.example.delivery.category.dto.ResGetCategoryDto;
import com.example.delivery.category.dto.ResCreateCategoryDto;
import com.example.delivery.category.entity.CategoryStatus;
import com.example.delivery.category.service.CategoryService;
import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.common.util.PageableFactory;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "카테고리", description = "카테고리 API")
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "카테고리 생성")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "카테고리 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 카테고리입니다."),
//            #TO-DO : 모든 인증이 필요한 API에 공통으로 적용되는 어노테이션을 별도로 생성하거나, 전역 필터 단에서 처리되도록 구조화하는 방식을 고려
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ResCreateCategoryDto>> createCategory(@Valid @RequestBody ReqCreateCategoryDto reqCreateCategoryDto){
        ResCreateCategoryDto resCreateCategoryDto = categoryService.createCategory(reqCreateCategoryDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("카테고리 생성 성공",resCreateCategoryDto));
//        #TO-DO :  성공 메시지를 하드코딩 하기 보다는, 별도의 SuccessCode 메시지 관리 Enum 클래스를 만들어 관리하는 것 고려
    }

    @Operation(summary = "전체 카테고리 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "전체 카테고리 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "정렬 기준이 올바르지 않습니다.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ResGetCategoryDto>>> getAllCategories(
            @RequestParam(defaultValue = "ACTIVE") CategoryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Pageable pageable = PageableFactory.of(page, size, sortBy, direction);
        Page<ResGetCategoryDto> categories = categoryService.getAllCategories(status, pageable);

        return ResponseEntity
                .ok(ApiResponse.success("전체 카테고리 목록 조회 성공", PageResponse.from(categories)));
    }

    @Operation(summary = "카테고리 상세 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "카테고리 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 타입이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<ResGetCategoryDto>> getCategory(@PathVariable UUID categoryId) {
        ResGetCategoryDto category = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("카테고리 상세 조회 성공", category));
    }
}
