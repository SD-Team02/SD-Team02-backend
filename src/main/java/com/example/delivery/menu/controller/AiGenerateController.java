package com.example.delivery.menu.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.menu.dto.request.AiGenerateRequestDto;
import com.example.delivery.menu.service.AiGenerateService;
import com.example.delivery.user.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AiGenerateController {

    private final AiGenerateService aiGenerateService;

    // AI 답변생성
    @PostMapping("/generate-description")
    @Tag(name = "AI 답변", description = "AI 답변 API")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> generateDescription(
            @RequestBody @Valid AiGenerateRequestDto aiGenerateRequestDto
//            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        log.info("AI 답변생성 getPrompt : " + aiGenerateRequestDto.getPrompt()
                + "menuName : " + aiGenerateRequestDto.getMenuName()
                + "getCategory : " + aiGenerateRequestDto.getCategory());

        String result = aiGenerateService.generateDescription(aiGenerateRequestDto/*, userDetails*/);
        log.info("AI 답변 result : " + result);
        return ResponseEntity.ok(
            ApiResponse.success("AI 답변생성이 완료되었습니다.", result)
        );
    }
}
