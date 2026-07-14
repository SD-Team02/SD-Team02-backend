package com.example.delivery.store.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.delivery.store.entity.Store;

public interface StoreRepository extends JpaRepository<Store, UUID> {

	// OWNER가 소유한(삭제되지 않은) 가게 목록
	List<Store> findByUserIdAndDeletedAtIsNull(Long userId);

	// 2026-07-14
	// menu 등록시 가게가 존재하는지 여부 조회
	Optional<Store> findByStoreIdAndDeletedAtIsNull(UUID storeId);
}
