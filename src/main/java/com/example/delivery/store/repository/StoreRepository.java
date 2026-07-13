package com.example.delivery.store.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.delivery.store.entity.Store;
import com.example.delivery.store.entity.StoreStatus;

public interface StoreRepository extends JpaRepository<Store, UUID> {

	// OWNER가 소유한(삭제되지 않은) 가게 목록
	List<Store> findByUserIdAndDeletedAtIsNull(Long userId);

	Optional<Store> findByName(String name);

	Page<Store> findByStatusAndDeletedAtIsNull(StoreStatus status, Pageable pageable);

	Optional<Store> findByStoreIdAndDeletedAtIsNull(UUID storeId);

	// keyword: 가게명 또는 (keywordCategoryIds로 매핑된) 카테고리명에 포함되는지 검색
	// categoryId: 특정 카테고리로 필터링 (null이면 미적용)
	@Query("SELECT s FROM Store s "
			+ "WHERE s.status = :status "
			+ "AND s.deletedAt IS NULL "
			+ "AND (:categoryId IS NULL OR s.categoryId = :categoryId) "
			+ "AND (:keyword IS NULL OR s.name LIKE CONCAT('%', :keyword, '%') OR s.categoryId IN :keywordCategoryIds)")
	Page<Store> searchStores(
			@Param("keyword") String keyword,
			@Param("categoryId") UUID categoryId,
			@Param("keywordCategoryIds") List<UUID> keywordCategoryIds,
			@Param("status") StoreStatus status,
			Pageable pageable);
}
