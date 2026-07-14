package com.example.delivery.review.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.delivery.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

	// 삭제되지 않은 리뷰 기준으로만 중복 여부/조회 (soft delete 제외)
	boolean existsByOrderIdAndDeletedAtIsNull(UUID orderId);

	Optional<Review> findByReviewIdAndDeletedAtIsNull(UUID reviewId);

	Page<Review> findByStoreIdAndDeletedAtIsNull(UUID storeId, Pageable pageable);
}
