package com.example.delivery.review.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.delivery.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

	boolean existsByOrderId(UUID orderId);

	Page<Review> findByStoreId(UUID storeId, Pageable pageable);
}
