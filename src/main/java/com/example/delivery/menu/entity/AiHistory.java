package com.example.delivery.menu.entity;

import java.util.UUID;

import com.example.delivery.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** AI 상품 설명 생성 요청/응답 로그. 수정/삭제가 없는 append-only 테이블이라 BaseTimeEntity를 상속한다. */
@Getter
@Entity
@Table(name = "p_ai_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ai_history_id")
    private UUID aiHistoryId;

    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    public AiHistory(String prompt, String response) {
        this.prompt = prompt;
        this.response = response;
    }
}
