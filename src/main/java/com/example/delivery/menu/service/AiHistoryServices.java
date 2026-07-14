package com.example.delivery.menu.service;

import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.menu.dto.request.AiHistoryRequestDto;
import com.example.delivery.menu.dto.response.AiHistoryResponseDto;
import com.example.delivery.menu.entity.AiHistory;
import com.example.delivery.menu.repository.AiHistoryRepository;
import com.example.delivery.user.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiHistoryServices {

    private final AiHistoryRepository aiHistoryRepository;

    public List<AiHistoryResponseDto> getAiHistoryList(Long createdBy, UserDetailsImpl userDetails) {

        List<AiHistory> histories = aiHistoryRepository.findTop3ByCreatedByOrderByCreatedAtDesc(createdBy);

        // 권한 확인
        // 2026-07-13
        // 코드리뷰 수정
        if (userDetails.getUser().getUserId().equals(histories.get(0).getCreatedBy())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return histories.stream()
                .map(AiHistoryResponseDto::from)
                .toList();
    }

    public PageResponse<AiHistoryResponseDto> getAminAiHistoryList(AiHistoryRequestDto aiHistoryRequestDto, UserDetailsImpl userDetails, Pageable pageable) {

        Page<AiHistory> histories = aiHistoryRepository.searchAdminAiHistory(aiHistoryRequestDto, pageable);
        Page<AiHistoryResponseDto> dtoPage = histories.map(AiHistoryResponseDto::new);
        return PageResponse.from(dtoPage);
    }

}
