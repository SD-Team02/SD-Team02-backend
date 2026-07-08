package com.example.delivery.menu.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class MenuRequestDto {
    private UUID menuId;                // 메뉴 아이디
    private UUID storeId;               // 가게 아이디
    private String storeName;           // 가게 이름
    private String menuName;            // 메뉴 이름
    private Integer price;              // 메뉴 가격
    private String description;         // 메뉴 설명
    private Boolean aiGenerated;        // AI 설명 생성 여부
    private String prompt;              // AI 에게 전달한 프롬프트
    private String response;            // Ai 응답 원문
    private Enum menuStatus;            // 메뉴 상태값 (정상: NORMAL,숨김: HIDDEN, 삭제: DELETE)
    private String searchDateType;      // 검색할 날짜 상태 값 (생성날짜, 수정날짜, 삭제날짜)
    private LocalDate minDate;          // 검색 날짜 의 최솟 값
    private LocalDate maxDate;          // 검색 날짜 의 최댓 값
    private String sortBy;              // 내림차순 오름차순 기준 값 (메뉴 이름, 메뉴 가격, 생성날짜, 수정날짜, 삭제날짜)
    private String orderType;           // 내림차순 오름차순(ASC, DESC)

}
