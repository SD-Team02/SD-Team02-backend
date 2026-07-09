package com.example.delivery.order.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.example.delivery.order.service.OrderService;

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

	@Operation(summary = "주문 생성")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문이 생성되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값이 올바르지 않습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가게 또는 메뉴를 찾을 수 없습니다."),
		// TODO : 인증이 필요한 API 공통 처리(어노테이션/필터) 구조화 고려
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping
	public ResponseEntity<ApiResponse<ResCreateOrderDto>> createOrder(
		@Valid @RequestBody ReqCreateOrderDto request
	) {
		// TODO : JWT 연동 후 인증 사용자 userId 전달
		ResCreateOrderDto response = orderService.createOrder(1L, request);

		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success("주문이 생성되었습니다.", response));
	}

	@Operation(summary = "주문 목록 조회")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 목록 조회가 되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "존재하지 않는 주문 상태입니다.")
	})
	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<ResOrderListDto>>> getOrders(
		@RequestParam(defaultValue = "ALL") String status,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(required = false) Integer size,
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@RequestParam(defaultValue = "DESC") String direction
	) {
		Pageable pageable = PageableFactory.of(page, size, sortBy, direction);
		PageResponse<ResOrderListDto> response = orderService.getOrders(status, pageable);

		return ResponseEntity
			.ok(ApiResponse.success("주문 목록 조회가 되었습니다.", response));
		// TODO : QueryDSL 적용 후 startDate/endDate 검색 파라미터 추가
	}

	@Operation(summary = "주문 단건 조회")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문이 조회되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없습니다.")
	})
	@GetMapping("/{orderId}")
	public ResponseEntity<ApiResponse<ResOrderDto>> getOrder(
		@PathVariable UUID orderId
	) {
		ResOrderDto response = orderService.getOrder(orderId);

		return ResponseEntity
			.ok(ApiResponse.success("주문이 조회되었습니다.", response));
	}

	@Operation(summary = "주문 상품 목록 조회")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 상품 목록이 조회되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없습니다.")
	})
	@GetMapping("/{orderId}/items")
	public ResponseEntity<ApiResponse<ResOrderItemsDto>> getOrderItems(
		@PathVariable UUID orderId
	) {
		ResOrderItemsDto response = orderService.getOrderItems(orderId);

		return ResponseEntity
			.ok(ApiResponse.success("주문 상품 목록이 조회되었습니다.", response));
	}

	@Operation(summary = "주문 상태 변경")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 상태가 변경되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "해당 상태로 변경할 수 없습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없습니다.")
	})
	@PatchMapping("/{orderId}/status")
	public ResponseEntity<ApiResponse<ResOrderStatusDto>> changeStatus(
		@PathVariable UUID orderId,
		@Valid @RequestBody ReqUpdateOrderStatusDto request
	) {
		ResOrderStatusDto response = orderService.changeStatus(orderId, request.getStatus());

		return ResponseEntity
			.ok(ApiResponse.success("주문 상태가 변경되었습니다.", response));
	}

	@Operation(summary = "주문 취소")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문이 취소되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "주문 취소 가능 시간(5분)이 지났습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 주문만 취소할 수 있습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없습니다.")
	})
	@PatchMapping("/{orderId}/cancel")
	public ResponseEntity<ApiResponse<ResOrderStatusDto>> cancelOrder(
		@PathVariable UUID orderId
	) {
		// TODO : JWT 연동 후 인증 사용자 userId 전달
		ResOrderStatusDto response = orderService.cancelOrder(1L, orderId);

		return ResponseEntity
			.ok(ApiResponse.success("주문이 취소되었습니다.", response));
	}

	@Operation(summary = "주문 삭제")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문이 삭제되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자만 삭제할 수 있습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없습니다.")
	})
	@DeleteMapping("/{orderId}")
	public ResponseEntity<ApiResponse<Void>> deleteOrder(
		@PathVariable UUID orderId
	) {
		// TODO : JWT 연동 후 인증 사용자 userId 전달
		orderService.deleteOrder(1L, orderId);

		return ResponseEntity
			.ok(ApiResponse.successMessage("주문이 삭제되었습니다."));
	}
}
