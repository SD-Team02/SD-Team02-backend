package com.example.delivery.global.common.response;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import lombok.Getter;

/**
 * 검색(Search) API 공통 응답. Page 또는 Slice 어느쪽을 쓰든 이 DTO로 변환해서 내려준다.
 * - Page 사용 시: totalElements/totalPages까지 함께 내려줌 (count 쿼리 발생)
 * - Slice 사용 시: totalElements/totalPages는 null, hasNext만 제공 (count 쿼리 없음, 대용량 목록에 유리)
 */
@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final int pageNumber;   // 0-based
    private final int pageSize;
    private final boolean hasNext;
    private final Long totalElements;   // Slice인 경우 null
    private final Integer totalPages;   // Slice인 경우 null

    private PageResponse(List<T> content, int pageNumber, int pageSize, boolean hasNext,
                         Long totalElements, Integer totalPages) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.hasNext = hasNext;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.hasNext(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public static <T> PageResponse<T> from(Slice<T> slice) {
        return new PageResponse<>(
                slice.getContent(),
                slice.getNumber(),
                slice.getSize(),
                slice.hasNext(),
                null,
                null
        );
    }
}