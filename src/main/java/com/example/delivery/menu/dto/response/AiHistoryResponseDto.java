package com.example.delivery.menu.dto.response;

import com.example.delivery.menu.entity.AiHistory;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class AiHistoryResponseDto {
    private String prompt;              // 메뉴 설명
    private String response;            // AI 답변
    private LocalDateTime createdAt;    // 생성날짜
    private Long createdBy;             // 생성자

    public AiHistoryResponseDto(AiHistory aiHistory) {
        this.prompt = aiHistory.getPrompt();
        this.response = aiHistory.getResponse();
        this.createdAt = aiHistory.getCreatedAt();
        this.createdBy = aiHistory.getCreatedBy();

    }

    public static AiHistoryResponseDto from(AiHistory aiHistory) {
        return new AiHistoryResponseDto(aiHistory);
    }
}
