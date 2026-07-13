package com.example.delivery.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class AiHistoryRequestDto {

    private LocalDate minDate;          // 검색 날짜 의 최솟 값
    private LocalDate maxDate;          // 검색 날짜 의 최댓 값
    private String sortBy;              // 내림차순 오름차순 기준 값 (유저 이름, 생성날짜)
    private String orderType;           // 내림차순 오름차순(ASC, DESC)
    private Long createdBy;

}
