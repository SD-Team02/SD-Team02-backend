package com.example.delivery.category.controller;

import com.example.delivery.category.dto.ReqCreateCategoryDto;
import com.example.delivery.category.dto.ResCreateCategoryDto;
import com.example.delivery.category.service.CategoryService;
import com.example.delivery.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "카테고리", description = "카테고리 API")
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "카테고리 생성")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "카테고리 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody ReqCreateCategoryDto reqCreateCategoryDto){
        ResCreateCategoryDto resCreateCategoryDto = categoryService.createCategory(reqCreateCategoryDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(resCreateCategoryDto));
    }
}
