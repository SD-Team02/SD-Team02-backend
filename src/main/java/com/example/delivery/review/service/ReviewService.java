package com.example.delivery.review.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderStatus;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.review.dto.request.ReqCreateReviewDto;
import com.example.delivery.review.dto.request.ReqUpdateReviewDto;
import com.example.delivery.review.dto.response.ResCreateReviewDto;
import com.example.delivery.review.dto.response.ResReviewDto;
import com.example.delivery.review.dto.response.ResReviewListDto;
import com.example.delivery.review.dto.response.ResUpdateReviewDto;
import com.example.delivery.review.entity.Review;
import com.example.delivery.review.repository.ReviewRepository;
import com.example.delivery.store.repository.StoreRepository;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;

	private final OrderRepository orderRepository;
	private final StoreRepository storeRepository;
	private final UserRepository userRepository;

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


	@Transactional(readOnly = true)
	public PageResponse<ResReviewListDto> getStoreReviews(UUID storeId, Pageable pageable) {

		// 가게 존재 검증
		storeRepository.findById(storeId)
			.orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

		Page<ResReviewListDto> reviews = reviewRepository.findByStoreId(storeId, pageable)
			.map(review -> {

				User user = userRepository.findById(review.getUserId())
					.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

				return ResReviewListDto.builder()
					.reviewId(review.getReviewId())
					.username(user.getUsername())
					.rating(review.getRating())
					.content(review.getContent())
					.createdAt(review.getCreatedAt())
					.build();
			});

		return PageResponse.from(reviews);

		// TODO : username N+1 조회 -> 배치 조회/QueryDSL로 최적화
		// TODO : QueryDSL 적용 후 startDate/endDate 조건 검색 추가
	}


	@Transactional(readOnly = true)
	public ResReviewDto getReview(UUID reviewId) {

		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

		User user = userRepository.findById(review.getUserId())
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		return ResReviewDto.builder()
			.reviewId(review.getReviewId())
			.storeId(review.getStoreId())
			.username(user.getUsername())
			.rating(review.getRating())
			.content(review.getContent())
			.createdAt(review.getCreatedAt())
			.build();
	}


	@Transactional
	public ResUpdateReviewDto updateReview(Long userId, UUID reviewId, ReqUpdateReviewDto request) {

		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

		// 본인 리뷰만 수정 가능 (CUSTOMER)
		if (!review.getUserId().equals(userId)) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		// 변경분은 dirty checking으로 반영
		review.update(request.getRating(), request.getContent());

		return new ResUpdateReviewDto(review.getReviewId());
	}


	@Transactional
	public void deleteReview(Long userId, UUID reviewId) {

		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

		review.softDelete(userId);

		// TODO : JWT 연동 후 권한 검증 (CUSTOMER는 본인 리뷰만 / MANAGER·MASTER는 전체)
	}
}
