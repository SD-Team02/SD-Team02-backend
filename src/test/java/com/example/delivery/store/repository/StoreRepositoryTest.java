package com.example.delivery.store.repository;

import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.global.config.QuerydslConfig;
import com.example.delivery.store.entity.Store;
import com.example.delivery.store.entity.StoreStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
// QueryDSL을 쓰기 위해 QuerydslConfig.class도 같이 import 해줘야 함
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
class StoreRepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("searchStores - keywordCategoryIds가 빈 리스트여도 예외 없이 조회된다")
    void searchStores_EmptyKeywordCategoryIds_DoesNotThrow() {
        // given
        Store store = new Store(1L, UUID.randomUUID(), UUID.randomUUID(), "가나다", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));
        storeRepository.save(store);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Store> result = storeRepository.searchStores(
                "매칭안됨", null, List.of(), StoreStatus.OPEN, pageable);

        // then
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("searchStores - keywordCategoryIds에 매칭되는 카테고리로 검색되면 결과가 나온다")
    void searchStores_MatchesByKeywordCategoryIds() {
        // given
        UUID categoryId = UUID.randomUUID();
        Store store = new Store(1L, categoryId, UUID.randomUUID(), "매칭안되는이름", "주소",
                "010-1234-5678", LocalTime.of(9, 0), LocalTime.of(22, 0));
        storeRepository.save(store);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Store> result = storeRepository.searchStores(
                "아무키워드", null, List.of(categoryId), StoreStatus.OPEN, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}