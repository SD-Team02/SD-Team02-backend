package com.example.delivery.menu.repository;

import com.example.delivery.menu.dto.request.AiHistoryRequestDto;
import com.example.delivery.menu.entity.AiHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AiHistoryRepositoryCustom {
    Page<AiHistory> searchAdminAiHistory(AiHistoryRequestDto aiHistoryRequestDto, Pageable pageable);
}
