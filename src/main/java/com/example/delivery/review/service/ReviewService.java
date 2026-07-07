package com.example.delivery.review.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderStatus;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.review.dto.request.ReqCreateReviewDto;
import com.example.delivery.review.dto.response.ResCreateReviewDto;
import com.example.delivery.review.entity.Review;
import com.example.delivery.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;

	private final OrderRepository orderRepository;

	@Transactional
	public ResCreateReviewDto createReview(Long userId, UUID orderId, ReqCreateReviewDto request) {

		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

		if (!order.getUserId().equals(userId)) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.COMPLETED) {
			throw new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED);
		}

		if (reviewRepository.existsByOrderId(orderId)) {
			throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
		}

		Review review = new Review(
			order.getStoreId(),
			orderId,
			userId,
			request.getRating(),
			request.getContent()
		);

		reviewRepository.save(review);

		return new ResCreateReviewDto(review.getReviewId());

		// TODO : JWT 연동 후 CUSTOMER 권한 검증
	}
}
