package com.example.delivery.review.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.common.util.PageableFactory;
import com.example.delivery.review.dto.request.ReqCreateReviewDto;
import com.example.delivery.review.dto.request.ReqUpdateReviewDto;
import com.example.delivery.review.dto.response.ResCreateReviewDto;
import com.example.delivery.review.dto.response.ResReviewDto;
import com.example.delivery.review.dto.response.ResReviewListDto;
import com.example.delivery.review.dto.response.ResUpdateReviewDto;
import com.example.delivery.review.service.ReviewService;
import com.example.delivery.user.security.UserDetailsImpl;
import com.example.delivery.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "리뷰", description = "리뷰 API")
public class ReviewController {

	private final ReviewService reviewService;
	private final UserService userService;

	@Operation(summary = "리뷰 생성")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "리뷰가 생성되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "리뷰를 작성할 수 없는 주문이거나 입력값이 올바르지 않습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 주문만 리뷰할 수 있습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 리뷰가 존재합니다.")
	})
	@PreAuthorize("hasAuthority('CUSTOMER')")
	@PostMapping("/api/orders/{orderId}/reviews")
	public ResponseEntity<ApiResponse<ResCreateReviewDto>> createReview(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable UUID orderId,
		@Valid @RequestBody ReqCreateReviewDto request
	) {
		// 본인 주문 여부 / 배달완료 상태 / 중복 리뷰는 서비스에서 검증
		Long userId = userService.getCurrentUserId(userDetails);
		ResCreateReviewDto response = reviewService.createReview(userId, orderId, request);

		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success("리뷰가 생성되었습니다.", response));
	}

	@Operation(summary = "가게 리뷰 목록 조회")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 목록이 조회되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가게를 찾을 수 없습니다.")
	})
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/api/stores/{storeId}/reviews")
	public ResponseEntity<ApiResponse<PageResponse<ResReviewListDto>>> getStoreReviews(
		@PathVariable UUID storeId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(required = false) Integer size,
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@RequestParam(defaultValue = "DESC") String direction
	) {
		Pageable pageable = PageableFactory.of(page, size, sortBy, direction);
		PageResponse<ResReviewListDto> response = reviewService.getStoreReviews(storeId, pageable);

		return ResponseEntity
			.ok(ApiResponse.success("리뷰 목록이 조회되었습니다.", response));
	}

	@Operation(summary = "리뷰 단건 조회")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰가 조회되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없습니다.")
	})
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/api/reviews/{reviewId}")
	public ResponseEntity<ApiResponse<ResReviewDto>> getReview(
		@PathVariable UUID reviewId
	) {
		ResReviewDto response = reviewService.getReview(reviewId);

		return ResponseEntity
			.ok(ApiResponse.success("리뷰가 조회되었습니다.", response));
	}

	@Operation(summary = "리뷰 수정")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰가 수정되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값이 올바르지 않습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 리뷰만 수정할 수 있습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없습니다.")
	})
	@PreAuthorize("hasAuthority('CUSTOMER')")
	@PatchMapping("/api/reviews/{reviewId}")
	public ResponseEntity<ApiResponse<ResUpdateReviewDto>> updateReview(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable UUID reviewId,
		@Valid @RequestBody ReqUpdateReviewDto request
	) {
		// 본인 리뷰 여부는 서비스에서 검증
		Long userId = userService.getCurrentUserId(userDetails);
		ResUpdateReviewDto response = reviewService.updateReview(userId, reviewId, request);

		return ResponseEntity
			.ok(ApiResponse.success("리뷰가 수정되었습니다.", response));
	}

	@Operation(summary = "리뷰 삭제")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰가 삭제되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "삭제 권한이 없습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없습니다.")
	})
	@PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER', 'MASTER')")
	@DeleteMapping("/api/reviews/{reviewId}")
	public ResponseEntity<ApiResponse<Void>> deleteReview(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable UUID reviewId
	) {
		// CUSTOMER는 본인 리뷰만 / MANAGER·MASTER는 전체 (소유권 검증은 서비스에서 처리)
		Long userId = userService.getCurrentUserId(userDetails);
		reviewService.deleteReview(userId, reviewId);

		return ResponseEntity
			.ok(ApiResponse.successMessage("리뷰가 삭제되었습니다."));
	}
}
