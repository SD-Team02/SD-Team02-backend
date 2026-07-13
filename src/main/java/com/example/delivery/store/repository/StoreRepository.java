package com.example.delivery.store.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.delivery.store.entity.Store;

public interface StoreRepository extends JpaRepository<Store, UUID> {

	// OWNER가 소유한(삭제되지 않은) 가게 목록
	List<Store> findByUserIdAndDeletedAtIsNull(Long userId);

	Optional<Store> findByName(String name);
}
