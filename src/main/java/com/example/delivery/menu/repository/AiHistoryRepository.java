package com.example.delivery.menu.repository;

import com.example.delivery.menu.entity.AiHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiHistoryRepository extends JpaRepository<AiHistory, UUID> {
}
