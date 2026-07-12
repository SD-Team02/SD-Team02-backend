package com.example.delivery.menu.service;

import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.menu.dto.request.AiHistoryRequestDto;
import com.example.delivery.menu.dto.response.AiHistoryResponseDto;
import com.example.delivery.menu.entity.AiHistory;
import com.example.delivery.menu.repository.AiHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiHistoryServices {

    private final AiHistoryRepository aiHistoryRepository;

    public List<AiHistoryResponseDto> getAiHistoryList(Long createdBy) {

        List<AiHistory> histories = aiHistoryRepository.findTop3ByCreatedByOrderByCreatedAtDesc(createdBy);

        return histories.stream()
                .map(AiHistoryResponseDto::from)
                .toList();
    }

    public PageResponse<AiHistoryResponseDto> getAminAiHistoryList(AiHistoryRequestDto aiHistoryRequestDto, JpaAuditingConfig.CustomUserDetails userDetails, Pageable pageable) {
        // 권한 확인 (주석 처리된 부분 - 나중에 활성화)
        //if ("CUSTOMER".equals(userDetails.getRole()) || "OWNER".equals(userDetails.getRole())) {
        //    throw new BusinessException(ErrorCode.ACCESS_DENIED);
        //}
        Page<AiHistory> histories = aiHistoryRepository.searchAdminAiHistory(aiHistoryRequestDto, pageable);
        Page<AiHistoryResponseDto> dtoPage = histories.map(AiHistoryResponseDto::new);
        return PageResponse.from(dtoPage);
    }

}
