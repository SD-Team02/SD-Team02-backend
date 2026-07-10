package com.example.delivery.order.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.common.util.PageableFactory;
import com.example.delivery.order.dto.request.ReqCreateOrderDto;
import com.example.delivery.order.dto.request.ReqUpdateOrderStatusDto;
import com.example.delivery.order.dto.response.ResCreateOrderDto;
import com.example.delivery.order.dto.response.ResOrderDto;
import com.example.delivery.order.dto.response.ResOrderItemsDto;
import com.example.delivery.order.dto.response.ResOrderListDto;
import com.example.delivery.order.dto.response.ResOrderStatusDto;
import com.example.delivery.order.entity.OrderStatus;
import com.example.delivery.order.service.OrderService;
import com.example.delivery.user.entity.Role;
import com.example.delivery.user.security.UserDetailsImpl;
import com.example.delivery.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "주문", description = "주문 API")
public class OrderController {

	private final OrderService orderService;
	private final UserService userService;

	@Operation(summary = "주문 생성")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문이 생성되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값이 올바르지 않습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가게 또는 메뉴를 찾을 수 없습니다.")
	})
	@PreAuthorize("hasAuthority('CUSTOMER')")
	@PostMapping
	public ResponseEntity<ApiResponse<ResCreateOrderDto>> createOrder(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@Valid @RequestBody ReqCreateOrderDto request
	) {
		Long userId = userService.getCurrentUserId(userDetails);
		ResCreateOrderDto response = orderService.createOrder(userId, request);

		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success("주문이 생성되었습니다.", response));
	}

	@Operation(summary = "주문 목록 조회")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 목록 조회가 되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "존재하지 않는 주문 상태입니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PreAuthorize("isAuthenticated()")
	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<ResOrderListDto>>> getOrders(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		// status 미지정 시 전체 상태 조회, 지정 시 해당 상태만. 잘못된 값은 스프링 바인딩 단계에서 400 처리.
		@RequestParam(required = false) OrderStatus status,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(required = false) Integer size,
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@RequestParam(defaultValue = "DESC") String direction
	) {
		// 역할별 조회 범위(CUSTOMER 본인 / OWNER 본인 가게 / MANAGER·MASTER 전체)는 서비스에서 처리
		Long userId = userService.getCurrentUserId(userDetails);
		Role role = userDetails.getUser().getRole();

		Pageable pageable = PageableFactory.of(page, size, sortBy, direction);
		PageResponse<ResOrderListDto> response = orderService.getOrders(userId, role, status, pageable);

		return ResponseEntity
			.ok(ApiResponse.success("주문 목록 조회가 되었습니다.", response));
		// TODO : QueryDSL 적용 후 startDate/endDate 검색 파라미터 추가
	}

	@Operation(summary = "주문 단건 조회")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문이 조회되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "조회 권한 없음"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없습니다.")
	})
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/{orderId}")
	public ResponseEntity<ApiResponse<ResOrderDto>> getOrder(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable UUID orderId
	) {
		Long userId = userService.getCurrentUserId(userDetails);
		Role role = userDetails.getUser().getRole();

		ResOrderDto response = orderService.getOrder(userId, role, orderId);

		return ResponseEntity
			.ok(ApiResponse.success("주문이 조회되었습니다.", response));
	}

	@Operation(summary = "주문 상품 목록 조회")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 상품 목록이 조회되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "조회 권한 없음"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없습니다.")
	})
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/{orderId}/items")
	public ResponseEntity<ApiResponse<ResOrderItemsDto>> getOrderItems(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable UUID orderId
	) {
		Long userId = userService.getCurrentUserId(userDetails);
		Role role = userDetails.getUser().getRole();

		ResOrderItemsDto response = orderService.getOrderItems(userId, role, orderId);

		return ResponseEntity
			.ok(ApiResponse.success("주문 상품 목록이 조회되었습니다.", response));
	}

	@Operation(summary = "주문 상태 변경")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 상태가 변경되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "해당 상태로 변경할 수 없습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없습니다.")
	})
	@PreAuthorize("hasAnyAuthority('OWNER', 'MANAGER', 'MASTER')")
	@PatchMapping("/{orderId}/status")
	public ResponseEntity<ApiResponse<ResOrderStatusDto>> changeStatus(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable UUID orderId,
		@Valid @RequestBody ReqUpdateOrderStatusDto request
	) {
		// OWNER는 본인 가게 주문만 변경 가능(소유권은 서비스에서 검증)
		Long userId = userService.getCurrentUserId(userDetails);
		Role role = userDetails.getUser().getRole();

		ResOrderStatusDto response = orderService.changeStatus(userId, role, orderId, request.getStatus());

		return ResponseEntity
			.ok(ApiResponse.success("주문 상태가 변경되었습니다.", response));
	}

	@Operation(summary = "주문 취소")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문이 취소되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "주문 취소 가능 시간(5분)이 지났습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 주문만 취소할 수 있습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없습니다.")
	})
	@PreAuthorize("hasAuthority('CUSTOMER')")
	@PatchMapping("/{orderId}/cancel")
	public ResponseEntity<ApiResponse<ResOrderStatusDto>> cancelOrder(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable UUID orderId
	) {
		// 본인 주문 여부는 서비스에서 검증 (order.userId == 요청자)
		Long userId = userService.getCurrentUserId(userDetails);
		ResOrderStatusDto response = orderService.cancelOrder(userId, orderId);

		return ResponseEntity
			.ok(ApiResponse.success("주문이 취소되었습니다.", response));
	}

	@Operation(summary = "주문 삭제")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문이 삭제되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자만 삭제할 수 있습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없습니다.")
	})
	@PreAuthorize("hasAnyAuthority('MANAGER', 'MASTER')")
	@DeleteMapping("/{orderId}")
	public ResponseEntity<ApiResponse<Void>> deleteOrder(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable UUID orderId
	) {
		Long userId = userService.getCurrentUserId(userDetails);
		orderService.deleteOrder(userId, orderId);

		return ResponseEntity
			.ok(ApiResponse.successMessage("주문이 삭제되었습니다."));
	}
}
