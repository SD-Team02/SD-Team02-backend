package com.example.delivery.global.common.util;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 모든 도메인의 Search API가 공통으로 사용하는 페이징 규칙.
 *
 * 요구사항:
 * - 정렬 기본값은 생성일(createdAt) 기준
 * - 페이지 노출 건수는 10 / 30 / 50 만 허용, 그 외 값은 기본 10건으로 고정
 *
 * 사용 예 (Controller):
 *   @GetMapping("/stores")
 *   public ResponseEntity<ApiResponse<PageResponse<StoreResponse>>> search(
 *           @RequestParam(defaultValue = "0") int page,
 *           @RequestParam(required = false) Integer size,
 *           @RequestParam(defaultValue = "createdAt") String sortBy,
 *           @RequestParam(defaultValue = "DESC") String direction) {
 *       Pageable pageable = PageableFactory.of(page, size, sortBy, direction);
 *       ...
 *   }
 */
public final class PageableFactory {

    private static final Set<Integer> ALLOWED_SIZES = Set.of(10, 30, 50);
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_PROPERTY = "createdAt";

    private PageableFactory() {
    }

    public static Pageable of(int page, Integer size) {
        return of(page, size, DEFAULT_SORT_PROPERTY, Sort.Direction.DESC.name());
    }

    public static Pageable of(int page, Integer size, String sortBy, String direction) {
        int safePage = Math.max(page, 0);
        int safeSize = resolveSize(size);
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_PROPERTY : sortBy;
        Sort.Direction safeDirection = parseDirection(direction);

        return PageRequest.of(safePage, safeSize, Sort.by(safeDirection, safeSortBy));
    }

    private static int resolveSize(Integer requestedSize) {
        if (requestedSize == null || !ALLOWED_SIZES.contains(requestedSize)) {
            return DEFAULT_SIZE;
        }
        return requestedSize;
    }

    private static Sort.Direction parseDirection(String direction) {
        try {
            return Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            return Sort.Direction.DESC;
        }
    }
}