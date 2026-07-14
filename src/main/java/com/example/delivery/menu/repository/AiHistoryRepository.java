package com.example.delivery.menu.repository;

import com.example.delivery.menu.entity.AiHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AiHistoryRepository extends JpaRepository<AiHistory, Long>, AiHistoryRepositoryCustom {
    List<AiHistory> findTop3ByCreatedByOrderByCreatedAtDesc(Long createdBy);

}
