package com.example.delivery.menu.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.menu.dto.request.AiGenerateRequestDto;
import com.example.delivery.menu.dto.request.AiHistoryRequestDto;
import com.example.delivery.menu.dto.response.AiHistoryResponseDto;
import com.example.delivery.menu.service.AiHistoryServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AiHistoryController {

    private final AiHistoryServices aiHistoryServices;

    // 사용자 AI 히스토리 검색
    @GetMapping("/aihistory")
    public ResponseEntity<ApiResponse<List<AiHistoryResponseDto>>> getAiHistoryList(
           @RequestParam Long createdBy){

        log.info("사용자 AI 히스토리 검색 createdBy : " + createdBy);
        List<AiHistoryResponseDto> aiHistoryList = aiHistoryServices.getAiHistoryList(createdBy);

        return ResponseEntity.ok(
                ApiResponse.success("AI 히스토리 조회가 되었습니다", aiHistoryList)
        );
    }
    // 관리자 AI 히스토리 검색
    @GetMapping("/aihistory/admin")
    public ResponseEntity<ApiResponse<PageResponse<AiHistoryResponseDto>>> getAminAiHistoryList(
           AiHistoryRequestDto aiHistoryRequestDto,
           @PageableDefault(size = 10) Pageable pageable,
           @AuthenticationPrincipal JpaAuditingConfig.CustomUserDetails userDetails){

        log.info("관리자 AI 히스토리 검색 aiRequestDto : " + aiHistoryRequestDto + "\nuserDetails : userDetails" + "\npageable : " + pageable);
        PageResponse<AiHistoryResponseDto> aiHistoryList = aiHistoryServices.getAminAiHistoryList(aiHistoryRequestDto, userDetails, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("AI 히스토리 조회가 되었습니다", aiHistoryList)
        );
    }
}
