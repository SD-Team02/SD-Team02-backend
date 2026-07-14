package com.example.delivery.menu.repository;

import com.example.delivery.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface MenuRepository extends JpaRepository<Menu, UUID>, MenuRepositoryCustom {

    // 메뉴 가져오기
    List<Menu> findByStoreIdAndDeletedAtIsNull(UUID storeId);

    Optional<Menu> findByMenuIdAndDeletedAtIsNull(UUID menuId);
}