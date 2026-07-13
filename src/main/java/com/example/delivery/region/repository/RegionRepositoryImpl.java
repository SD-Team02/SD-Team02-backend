package com.example.delivery.region.repository;

import static com.example.delivery.region.entity.QRegion.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.delivery.region.dto.response.ResGetRegionDto;
import com.example.delivery.region.entity.QRegion;
import com.example.delivery.region.entity.RegionStatus;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegionRepositoryImpl implements RegionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ResGetRegionDto> findAllWithParentName(RegionStatus status, Pageable pageable) {

        // 상위 지역 조회용 Region 두 번째 별칭 (self-join)
        QRegion parent = new QRegion("parent");

        List<Tuple> rows = queryFactory
                .select(region, parent.name)
                .from(region)
                // parentRegionId는 연관관계 매핑이 아니라 raw ID라 ON 조건으로 self-join
                .leftJoin(parent).on(
                        region.parentRegionId.eq(parent.regionId)
                                .and(parent.deletedAt.isNull()))
                .where(
                        statusEq(status),
                        region.deletedAt.isNull()
                )
                .orderBy(toOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<ResGetRegionDto> content = rows.stream()
                .map(row -> ResGetRegionDto.from(row.get(region), row.get(parent.name)))
                .toList();

        Long total = queryFactory
                .select(region.count())
                .from(region)
                .where(
                        statusEq(status),
                        region.deletedAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression statusEq(RegionStatus status) {
        return status != null ? region.status.eq(status) : null;
    }


    // Pageable의 정렬을 QueryDSL OrderSpecifier로 변환 (허용 필드만, 기본값 createdAt DESC)
    private OrderSpecifier<?>[] toOrderSpecifiers(Pageable pageable) {
        if (pageable.getSort().isEmpty()) {
            return new OrderSpecifier<?>[]{ region.createdAt.desc() };
        }

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        for (Sort.Order sortOrder : pageable.getSort()) {
            Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;
            OrderSpecifier<?> spec = switch (sortOrder.getProperty()) {
                case "name" -> new OrderSpecifier<>(direction, region.name);
                case "updatedAt" -> new OrderSpecifier<>(direction, region.updatedAt);
                default -> new OrderSpecifier<>(direction, region.createdAt);
            };
            orders.add(spec);
        }
        return orders.toArray(new OrderSpecifier<?>[0]);
    }
}
