package com.example.delivery.review.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
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
import com.example.delivery.user.entity.Role;
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

		if (reviewRepository.existsByOrderIdAndDeletedAtIsNull(orderId)) {
			throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
		}

		Review review = new Review(
				order.getStoreId(),
				orderId,
				userId,
				request.getRating(),
				request.getContent()
		);

		// 최종 방어선은 DB 부분 유니크 인덱스(order_id where deleted_at is null)이며,
		// 위반 시 saveAndFlush에서 예외가 발생하므로 중복 에러로 변환한다.
		try {
			reviewRepository.saveAndFlush(review);
		} catch (DataIntegrityViolationException e) {
			throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
		}

		return new ResCreateReviewDto(review.getReviewId());
	}


	@Transactional(readOnly = true)
	public PageResponse<ResReviewListDto> getStoreReviews(UUID storeId, Pageable pageable) {

		// 가게 존재 검증
		storeRepository.findById(storeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

		Page<Review> reviewPage = reviewRepository.findByStoreIdAndDeletedAtIsNull(storeId, pageable);

		// 페이지 내 작성자를 한 번에 조회해 Map으로 재사용 (username 건별 조회 N+1 방지)
		List<Long> userIds = reviewPage.getContent().stream()
				.map(Review::getUserId)
				.distinct()
				.toList();

		Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
				.collect(Collectors.toMap(User::getUserId, Function.identity()));

		Page<ResReviewListDto> reviews = reviewPage.map(review -> {

			User user = userMap.get(review.getUserId());
			if (user == null) {
				throw new BusinessException(ErrorCode.USER_NOT_FOUND);
			}

			return ResReviewListDto.builder()
					.reviewId(review.getReviewId())
					.username(user.getUsername())
					.rating(review.getRating())
					.content(review.getContent())
					.createdAt(review.getCreatedAt())
					.build();
		});

		return PageResponse.from(reviews);

		// TODO : QueryDSL 적용 후 startDate/endDate 조건 검색 추가
	}


	@Transactional(readOnly = true)
	public ResReviewDto getReview(UUID reviewId) {

		Review review = reviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)
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

		Review review = reviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)
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
	public void deleteReview(Long userId, Role role, UUID reviewId) {

		Review review = reviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)
				.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

		// CUSTOMER는 본인 리뷰만, MANAGER·MASTER는 전체 삭제 가능
		if (role == Role.CUSTOMER && !review.getUserId().equals(userId)) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		review.softDelete(userId);
	}
}
