package com.example.delivery.category.controller;

import com.example.delivery.category.dto.request.ReqCreateCategoryDto;
import com.example.delivery.category.dto.request.ReqUpdateCategoryDto;
import com.example.delivery.category.dto.response.ResCreateCategoryDto;
import com.example.delivery.category.dto.response.ResDeleteCategoryDto;
import com.example.delivery.category.dto.response.ResGetCategoryDto;
import com.example.delivery.category.dto.response.ResUpdateCategoryDto;
import com.example.delivery.category.entity.CategoryStatus;
import com.example.delivery.category.service.CategoryService;
import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.common.util.PageableFactory;
import com.example.delivery.user.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("hasAnyAuthority('MANAGER', 'MASTER')")
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
        Page<ResGetCategoryDto> resCategories = categoryService.getAllCategories(status, pageable);

        return ResponseEntity
                .ok(ApiResponse.success("전체 카테고리 목록 조회 성공", PageResponse.from(resCategories)));
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
        ResGetCategoryDto resGetCategoryDto = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("카테고리 상세 조회 성공", resGetCategoryDto));
    }

    @Operation(summary = "카테고리 수정")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "카테고리 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 타입이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 카테고리입니다."),
            //            #TO-DO : 현재 카테고리명 또는 상태 값을 안 넣으면 "입력값이 올바르지 않습니다." 메세지 출력됨
//            -> 추후 GlobalExceptionHandler의 handleValidationException 부분 코드를 변경해 어떤 필드 값이 안 들어가는지 보여주도록 수정하는 것도 고려하면 좋을 듯
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "카테고리명은 필수입니다."),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "카테고리 상태는 필수입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문을 읽을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PreAuthorize("hasAnyAuthority('MANAGER', 'MASTER')")
    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<ResUpdateCategoryDto>> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody ReqUpdateCategoryDto reqUpdateCategoryDto){
        ResUpdateCategoryDto resUpdateCategoryDto = categoryService.updateCategory(categoryId, reqUpdateCategoryDto);
        return ResponseEntity.ok(ApiResponse.success("카테고리 수정 성공", resUpdateCategoryDto));
    }

    @Operation(summary = "카테고리 삭제")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "카테고리 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 타입이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PreAuthorize("hasAnyAuthority('MANAGER', 'MASTER')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<ResDeleteCategoryDto>> deleteCategory(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getUserId();
        ResDeleteCategoryDto resDeleteCategoryDto = categoryService.deleteCategory(categoryId, userId);
        return ResponseEntity.ok(ApiResponse.success("카테고리 삭제 성공", resDeleteCategoryDto));
    }
}
