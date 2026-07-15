package com.example.delivery.menu.repository;

import com.example.delivery.menu.dto.request.AiHistoryRequestDto;
import com.example.delivery.menu.entity.AiHistory;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
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

import static com.example.delivery.menu.entity.QAiHistory.aiHistory;
import static com.example.delivery.menu.entity.QMenu.menu;
import static com.example.delivery.user.entity.QUser.user;

@RequiredArgsConstructor
public class AiHistoryRepositoryImpl implements AiHistoryRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AiHistory> searchAdminAiHistory(AiHistoryRequestDto aiHistoryRequestDto, Pageable pageable){

        List<AiHistory> content = queryFactory
                .select(aiHistory)
                .join(user).on(aiHistory.createdBy.eq(user.userId))
                .where(
                    createByEq(aiHistoryRequestDto.getCreatedBy()),
                    dateRange(aiHistoryRequestDto.getMinDate(), aiHistoryRequestDto.getMaxDate())
                )
                .orderBy(getOrderSpecifier(aiHistoryRequestDto.getOrderType(), aiHistoryRequestDto.getSortBy()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long  total  = queryFactory
                .select(aiHistory.count())
                .from(aiHistory)
                .join(user).on(aiHistory.createdBy.eq(user.userId))
                .where(
                    createByEq(aiHistoryRequestDto.getCreatedBy()),
                    dateRange(aiHistoryRequestDto.getMinDate(), aiHistoryRequestDto.getMaxDate())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression createByEq(Long createdBy) {
        return createdBy != null ? aiHistory.createdBy.eq(createdBy) : null;
    }

    // 날짜 컬럼에 범위 조건을 걸지 분기
    private BooleanExpression dateRange(LocalDate minDate, LocalDate maxDate) {
        if (minDate == null && maxDate == null) {
            return null;
        }

        LocalDateTime min = minDate != null ? minDate.atStartOfDay() : null;
        LocalDateTime max = maxDate != null ? maxDate.plusDays(1).atStartOfDay() : null; // 해당일 끝까지 포함

        if (min != null && max != null) {
            return aiHistory.createdAt.between(min, max);
        } else if (min != null) {
            return aiHistory.createdAt.goe(min);
        } else {
            return aiHistory.createdAt.loe(max);
        }
    }

    // orderType, sortBy에 따라 정렬 분기
    private OrderSpecifier<?> getOrderSpecifier(String orderType, String sortBy) {
        boolean isDesc = !"ASC".equalsIgnoreCase(orderType); // 기본값 DESC
        // 2026-07-15 nullPoint오류수정
        String sort = (sortBy == null || sortBy.isBlank()) ? "CREATED" : sortBy;

        ComparableExpressionBase<?> targetColumn = switch (sort) {
            case "USER_NICKNAME" -> user.nickname;
            case "CREATED" -> aiHistory.createdAt;
            default -> aiHistory.createdAt; // 기본 정렬 기준
        };
        return isDesc ? targetColumn.desc() : targetColumn.asc();
    }

}
