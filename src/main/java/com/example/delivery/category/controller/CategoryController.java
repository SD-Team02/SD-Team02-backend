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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 카테고리입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
//            ##TO-DO : SecurityConfig에서 전역적으로 인증을 처리한다면, 개별 컨트롤러에서 401 응답을 정의하는 것이 다소 중복일 수 있음
//                      모든 인증이 필요한 API에 공통으로 적용되는 어노테이션을 별도로 생성하거나, 전역 필터 단에서 처리되도록 구조화하는 방식을 고려할 필요 있어보임
    })
    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody ReqCreateCategoryDto reqCreateCategoryDto){
        ResCreateCategoryDto resCreateCategoryDto = categoryService.createCategory(reqCreateCategoryDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("카테고리 생성 성공",resCreateCategoryDto));

//        #TO-DO : 성공 메세지 하드코딩 대신 별도의 `SuccessCode` 메시지 관리 Enum 클래스를 만들어 관리하는 것 고려
    }
}
