package com.example.delivery.global.common.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.user.entity.Address;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class BaseEntityAuditTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void updatedAtAndUpdatedBy_areNull_onCreate() {
        Address address = new Address(1L, "서울시 중구", "101호");

        entityManager.persist(address);
        entityManager.flush();
        entityManager.clear();

        Address found = entityManager.find(Address.class, address.getAddressId());

        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getCreatedBy()).isNotNull();
        assertThat(found.getUpdatedAt()).isNull();
        assertThat(found.getUpdatedBy()).isNull();
    }

    @Test
    void updatedAtAndUpdatedBy_areFilled_onRealUpdate() {
        Address address = new Address(1L, "서울시 중구", "101호");
        entityManager.persist(address);
        entityManager.flush();
        entityManager.clear();

        Address found = entityManager.find(Address.class, address.getAddressId());
        found.update("서울시 종로구", "202호");
        entityManager.flush();
        entityManager.clear();

        Address updated = entityManager.find(Address.class, address.getAddressId());

        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedBy()).isNotNull();
    }
}
