package com.example.delivery.menu.repository;

import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.menu.dto.request.MenuRequestDto;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.entity.MenuStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.example.delivery.menu.entity.QMenu.menu;
import static com.example.delivery.store.entity.QStore.store;

@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Menu> searchAdminMenus(MenuRequestDto menuRequestDto, Pageable pageable) {

        List<Menu> content = queryFactory
                .select(menu)
                .join(store).on(menu.storeId.eq(store.storeId))
                .where(
                    storeIdEq(menuRequestDto.getStoreId()),
                    menuNameContains(menuRequestDto.getMenuName()),
                    storeNameContains(menuRequestDto.getStoreName()),
                    dateRange(menuRequestDto.getSearchDateType(), menuRequestDto.getMinDate(), menuRequestDto.getMaxDate()),
                    menuStatusEq(String.valueOf(menuRequestDto.getMenuStatus()))
                )
                .orderBy(getOrderSpecifier(menuRequestDto.getOrderType(), menuRequestDto.getSortBy()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long  total  = queryFactory
                .select(menu.count())
                .from(menu)
                .join(store).on(menu.storeId.eq(store.storeId))
                .where(
                    storeIdEq(menuRequestDto.getStoreId()),
                    menuNameContains(menuRequestDto.getMenuName()),
                    storeNameContains(menuRequestDto.getStoreName()),
                    dateRange(menuRequestDto.getSearchDateType(), menuRequestDto.getMinDate(), menuRequestDto.getMaxDate()),
                    menuStatusEq(String.valueOf(menuRequestDto.getMenuStatus()))
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression storeIdEq(UUID storeId) {
        return storeId != null ? menu.storeId.eq(storeId) : null;
    }

    private BooleanExpression menuNameContains(String menuName) {
        return StringUtils.hasText(menuName) ? menu.menuName.contains(menuName) : null;
    }

    private BooleanExpression storeNameContains(String storeName) {
        return StringUtils.hasText(storeName) ? store.name.contains(storeName) : null;
    }

    // searchDateType에 따라 어떤 날짜 컬럼에 범위 조건을 걸지 분기
    private BooleanExpression dateRange(String searchDateType, LocalDate minDate, LocalDate maxDate) {
        if (!StringUtils.hasText(searchDateType) || (minDate == null && maxDate == null)) {
            return null;
        }

        DateTimePath<LocalDateTime> targetColumn = switch (searchDateType) {
            case "UPDATED" -> menu.updatedAt;
            case "DELETED" -> menu.deletedAt;
            default -> menu.createdAt;
        };

        LocalDateTime min = minDate != null ? minDate.atStartOfDay() : null;
        LocalDateTime max = maxDate != null ? maxDate.plusDays(1).atStartOfDay() : null; // 해당일 끝까지 포함

        if (min != null && max != null) {
            return targetColumn.between(min, max);
        } else if (min != null) {
            return targetColumn.goe(min);
        } else {
            return targetColumn.loe(max);
        }
    }

    // 상태 필터링 - menuStatus 기준
    private BooleanExpression menuStatusEq(String menuStatus) {
        // 2026-07-13
        // 코드리뷰 수정
        if (!StringUtils.hasText(menuStatus) || menuStatus.equals("ALL") || menuStatus.equals("null")) {
            return null; // 필터 없음 - 전체 조회
        }

        if (menuStatus.equals("DELETED")) {
            return menu.deletedAt.isNotNull(); // 삭제된 것만 - deletedAt으로 판단
        }

        try {
            MenuStatus status = MenuStatus.valueOf(menuStatus);
            return menu.menuStatus.eq(status).and(menu.deletedAt.isNull());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_MENU_STATUS); // 잘못된 값 들어오면 명확한 에러
        }
    }

    // orderType, sortBy에 따라 정렬 분기
    private OrderSpecifier<?> getOrderSpecifier(String orderType, String sortBy) {
        boolean isDesc = !"ASC".equalsIgnoreCase(orderType); // 기본값 DESC
        // 2026-07-15 nullPoint오류수정
        String sort = (sortBy == null || sortBy.isBlank()) ? "CREATED" : sortBy;

        ComparableExpressionBase<?> targetColumn = switch (sort) {
            case "MENU_NAME" -> menu.menuName;
            case "PRICE" -> menu.price;
            case "CREATED" -> menu.createdAt;
            case "UPDATED" -> menu.updatedAt;
            case "DELETED" -> menu.deletedAt;
            default -> menu.createdAt; // 기본 정렬 기준
        };
        return isDesc ? targetColumn.desc() : targetColumn.asc();
    }
}
