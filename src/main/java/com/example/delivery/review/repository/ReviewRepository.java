package com.example.delivery.review.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.delivery.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

	boolean existsByOrderId(UUID orderId);
}
