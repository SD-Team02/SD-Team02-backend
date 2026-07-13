package com.example.delivery.review.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

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
import com.example.delivery.store.entity.Store;
import com.example.delivery.store.repository.StoreRepository;
import com.example.delivery.user.entity.Role;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private ReviewService reviewService;

	private final Pageable pageable = PageRequest.of(0, 10);

	private static final Logger log = LoggerFactory.getLogger(ReviewServiceTest.class);

	@BeforeEach
	void printTestName(TestInfo testInfo) {
		log.info("");
		log.info("========================================================");
		log.info("▶ 실행 테스트: {}", testInfo.getDisplayName());
		log.info("========================================================");
	}

	// ===== 헬퍼 =====

	private Order createOrder(UUID orderId, Long userId, UUID storeId, OrderStatus status) {
		Order order = new Order(userId, storeId, "주소", "상세주소", 20000);
		ReflectionTestUtils.setField(order, "orderId", orderId);
		ReflectionTestUtils.setField(order, "status", status);
		return order;
	}

	private Store createStore(UUID storeId) {
		Store store = new Store(1L, UUID.randomUUID(), UUID.randomUUID(),
			"테스트가게", "가게주소", "01000000000", LocalTime.of(9, 0), LocalTime.of(22, 0));
		ReflectionTestUtils.setField(store, "storeId", storeId);
		return store;
	}

	private User createUser(Long userId, String username) {
		User user = new User(username, "닉네임", "test@test.com", "password", Role.CUSTOMER, "01000000000");
		ReflectionTestUtils.setField(user, "userId", userId);
		return user;
	}

	private Review createReview(UUID reviewId, UUID storeId, UUID orderId, Long userId) {
		Review review = new Review(storeId, orderId, userId, 5, "맛있어요");
		ReflectionTestUtils.setField(review, "reviewId", reviewId);
		return review;
	}

	private ReqCreateReviewDto createReq(int rating, String content) {
		ReqCreateReviewDto req = new ReqCreateReviewDto();
		ReflectionTestUtils.setField(req, "rating", rating);
		ReflectionTestUtils.setField(req, "content", content);
		return req;
	}

	private ReqUpdateReviewDto updateReq(int rating, String content) {
		ReqUpdateReviewDto req = new ReqUpdateReviewDto();
		ReflectionTestUtils.setField(req, "rating", rating);
		ReflectionTestUtils.setField(req, "content", content);
		return req;
	}

	// ===== createReview =====

	@Test
	@DisplayName("리뷰 생성 - 성공 (배달완료 주문, 본인, 중복 없음)")
	void createReview_success() {
		UUID orderId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		Order order = createOrder(orderId, 1L, storeId, OrderStatus.DELIVERED);

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
		when(reviewRepository.existsByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(false);

		ResCreateReviewDto result = reviewService.createReview(1L, orderId, createReq(5, "맛있어요"));

		assertThat(result).isNotNull();

		ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
		verify(reviewRepository).saveAndFlush(captor.capture());
		log.info("저장된 리뷰 storeId={}, userId={}, rating={}",
			captor.getValue().getStoreId(), captor.getValue().getUserId(), captor.getValue().getRating());
		assertThat(captor.getValue().getStoreId()).isEqualTo(storeId);
		assertThat(captor.getValue().getUserId()).isEqualTo(1L);
		assertThat(captor.getValue().getRating()).isEqualTo(5);
	}

	@Test
	@DisplayName("리뷰 생성 - 성공 (배달 후 완료 상태 주문도 허용)")
	void createReview_success_completed() {
		UUID orderId = UUID.randomUUID();
		Order order = createOrder(orderId, 1L, UUID.randomUUID(), OrderStatus.COMPLETED);

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
		when(reviewRepository.existsByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(false);

		reviewService.createReview(1L, orderId, createReq(4, "좋아요"));

		verify(reviewRepository).saveAndFlush(any(Review.class));
	}

	@Test
	@DisplayName("리뷰 생성 - 동시 요청으로 DB 유니크 위반 시 중복 처리")
	void createReview_concurrentDuplicate() {
		UUID orderId = UUID.randomUUID();
		Order order = createOrder(orderId, 1L, UUID.randomUUID(), OrderStatus.DELIVERED);

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
		when(reviewRepository.existsByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(false);
		// 선체크는 통과했지만, 동시 삽입으로 부분 유니크 인덱스에 걸린 상황
		when(reviewRepository.saveAndFlush(any(Review.class)))
			.thenThrow(new DataIntegrityViolationException("duplicate order_id"));

		assertThatThrownBy(() -> reviewService.createReview(1L, orderId, createReq(5, "맛있어요")))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS);
	}

	@Test
	@DisplayName("리뷰 생성 - 주문 없음")
	void createReview_orderNotFound() {
		UUID orderId = UUID.randomUUID();
		when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reviewService.createReview(1L, orderId, createReq(5, "맛있어요")))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ORDER_NOT_FOUND);
	}

	@Test
	@DisplayName("리뷰 생성 - 본인 주문 아니면 거부")
	void createReview_notOrderOwner() {
		UUID orderId = UUID.randomUUID();
		// 주문 주인은 2L, 요청자는 1L
		Order order = createOrder(orderId, 2L, UUID.randomUUID(), OrderStatus.DELIVERED);
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		assertThatThrownBy(() -> reviewService.createReview(1L, orderId, createReq(5, "맛있어요")))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("리뷰 생성 - 배달완료 전이면 거부")
	void createReview_notDelivered() {
		UUID orderId = UUID.randomUUID();
		Order order = createOrder(orderId, 1L, UUID.randomUUID(), OrderStatus.REQUESTED);
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		assertThatThrownBy(() -> reviewService.createReview(1L, orderId, createReq(5, "맛있어요")))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.REVIEW_NOT_ALLOWED);
	}

	@Test
	@DisplayName("리뷰 생성 - 이미 리뷰 존재")
	void createReview_alreadyExists() {
		UUID orderId = UUID.randomUUID();
		Order order = createOrder(orderId, 1L, UUID.randomUUID(), OrderStatus.DELIVERED);
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
		when(reviewRepository.existsByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(true);

		assertThatThrownBy(() -> reviewService.createReview(1L, orderId, createReq(5, "맛있어요")))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS);
	}

	// ===== getStoreReviews =====

	@Test
	@DisplayName("가게 리뷰 목록 - 성공 (작성자 배치 조회)")
	void getStoreReviews_success() {
		UUID storeId = UUID.randomUUID();
		Review review = createReview(UUID.randomUUID(), storeId, UUID.randomUUID(), 1L);
		Page<Review> page = new PageImpl<>(List.of(review), pageable, 1);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(createStore(storeId)));
		when(reviewRepository.findByStoreIdAndDeletedAtIsNull(storeId, pageable)).thenReturn(page);
		when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(createUser(1L, "홍길동")));

		PageResponse<ResReviewListDto> result = reviewService.getStoreReviews(storeId, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getUsername()).isEqualTo("홍길동");
		// 작성자는 건별(findById)이 아닌 배치(findAllById)로 조회
		verify(userRepository).findAllById(List.of(1L));
		verify(userRepository, never()).findById(any());
	}

	@Test
	@DisplayName("가게 리뷰 목록 - 가게 없음")
	void getStoreReviews_storeNotFound() {
		UUID storeId = UUID.randomUUID();
		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reviewService.getStoreReviews(storeId, pageable))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.STORE_NOT_FOUND);
	}

	// ===== getReview =====

	@Test
	@DisplayName("리뷰 단건 - 성공")
	void getReview_success() {
		UUID reviewId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		Review review = createReview(reviewId, storeId, UUID.randomUUID(), 1L);

		when(reviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));
		when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L, "홍길동")));

		ResReviewDto result = reviewService.getReview(reviewId);

		assertThat(result.getReviewId()).isEqualTo(reviewId);
		assertThat(result.getUsername()).isEqualTo("홍길동");
		assertThat(result.getStoreId()).isEqualTo(storeId);
	}

	@Test
	@DisplayName("리뷰 단건 - 없음 (삭제 포함)")
	void getReview_notFound() {
		UUID reviewId = UUID.randomUUID();
		when(reviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reviewService.getReview(reviewId))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
	}

	@Test
	@DisplayName("리뷰 단건 - 작성자 없음")
	void getReview_userNotFound() {
		UUID reviewId = UUID.randomUUID();
		Review review = createReview(reviewId, UUID.randomUUID(), UUID.randomUUID(), 1L);
		when(reviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reviewService.getReview(reviewId))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
	}

	// ===== updateReview =====

	@Test
	@DisplayName("리뷰 수정 - 성공 (본인)")
	void updateReview_success() {
		UUID reviewId = UUID.randomUUID();
		Review review = createReview(reviewId, UUID.randomUUID(), UUID.randomUUID(), 1L);
		when(reviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));

		ResUpdateReviewDto result = reviewService.updateReview(1L, reviewId, updateReq(3, "그럭저럭"));

		assertThat(result.getReviewId()).isEqualTo(reviewId);
		// dirty checking 대상 필드가 실제로 변경되었는지 확인
		assertThat(review.getRating()).isEqualTo(3);
		assertThat(review.getContent()).isEqualTo("그럭저럭");
	}

	@Test
	@DisplayName("리뷰 수정 - 없음")
	void updateReview_notFound() {
		UUID reviewId = UUID.randomUUID();
		when(reviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reviewService.updateReview(1L, reviewId, updateReq(3, "그럭저럭")))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
	}

	@Test
	@DisplayName("리뷰 수정 - 본인 리뷰 아니면 거부")
	void updateReview_notOwner() {
		UUID reviewId = UUID.randomUUID();
		// 리뷰 주인은 2L, 요청자는 1L
		Review review = createReview(reviewId, UUID.randomUUID(), UUID.randomUUID(), 2L);
		when(reviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));

		assertThatThrownBy(() -> reviewService.updateReview(1L, reviewId, updateReq(3, "그럭저럭")))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	// ===== deleteReview =====

	@Test
	@DisplayName("리뷰 삭제 - CUSTOMER 본인 리뷰 soft delete")
	void deleteReview_customer_success() {
		UUID reviewId = UUID.randomUUID();
		Review review = createReview(reviewId, UUID.randomUUID(), UUID.randomUUID(), 1L);
		when(reviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));

		reviewService.deleteReview(1L, Role.CUSTOMER, reviewId);

		assertThat(review.isDeleted()).isTrue();
		verify(reviewRepository, never()).delete(any());
	}

	@Test
	@DisplayName("리뷰 삭제 - CUSTOMER 본인 리뷰 아니면 거부")
	void deleteReview_customer_notOwner() {
		UUID reviewId = UUID.randomUUID();
		// 리뷰 주인은 2L, 요청자는 1L
		Review review = createReview(reviewId, UUID.randomUUID(), UUID.randomUUID(), 2L);
		when(reviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));

		assertThatThrownBy(() -> reviewService.deleteReview(1L, Role.CUSTOMER, reviewId))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("리뷰 삭제 - MANAGER는 타인 리뷰도 삭제 가능")
	void deleteReview_manager_success() {
		UUID reviewId = UUID.randomUUID();
		// 리뷰 주인은 2L, 요청자(MANAGER)는 99L
		Review review = createReview(reviewId, UUID.randomUUID(), UUID.randomUUID(), 2L);
		when(reviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));

		reviewService.deleteReview(99L, Role.MANAGER, reviewId);

		assertThat(review.isDeleted()).isTrue();
	}

	@Test
	@DisplayName("리뷰 삭제 - 없음")
	void deleteReview_notFound() {
		UUID reviewId = UUID.randomUUID();
		when(reviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reviewService.deleteReview(1L, Role.CUSTOMER, reviewId))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
	}
}
