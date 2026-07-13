package com.example.delivery.category.repository;

import com.example.delivery.category.entity.Category;
import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("카테고리명으로 존재 여부 확인 - 존재할 때")
    void existsByName_whenExists_thenReturnTrue() {
        // given
        String name = "한식";
        categoryRepository.save(new Category(name));

        // when
        boolean exists = categoryRepository.existsByName(name);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("카테고리명으로 존재 여부 확인 - 존재하지 않을 때")
    void existsByName_whenNotExists_thenReturnFalse() {
        // given
        String name = "한식";

        // when
        boolean exists = categoryRepository.existsByName(name);

        // then
        assertThat(exists).isFalse();
    }
}
