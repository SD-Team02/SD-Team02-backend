package com.example.delivery.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.entity.MenuStatus;
import com.example.delivery.menu.repository.MenuRepository;
import com.example.delivery.order.dto.request.ReqCreateOrderDto;
import com.example.delivery.order.dto.request.ReqCreateOrderMenuDto;
import com.example.delivery.order.dto.response.ResCreateOrderDto;
import com.example.delivery.order.dto.response.ResOrderDto;
import com.example.delivery.order.dto.response.ResOrderItemDto;
import com.example.delivery.order.dto.response.ResOrderItemsDto;
import com.example.delivery.order.dto.response.ResOrderListDto;
import com.example.delivery.order.dto.response.ResOrderPageDto;
import com.example.delivery.order.dto.response.ResOrderStatusDto;
import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderItem;
import com.example.delivery.order.entity.OrderStatus;
import com.example.delivery.order.repository.OrderItemRepository;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.store.entity.Store;
import com.example.delivery.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;

	private final MenuRepository menuRepository;
	private final StoreRepository storeRepository;

	@Transactional
	public ResCreateOrderDto createOrder(Long userId, ReqCreateOrderDto request) {

		// 가게 검증
		Store store = storeRepository.findById(request.getStoreId())
			.orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

		// 총 금액
		int totalPrice = 0;

		for (ReqCreateOrderMenuDto menuDto : request.getMenuList()) {

			Menu menu = menuRepository.findById(menuDto.getMenuId())
				.orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

			// 다른 가게 메뉴인지 검증
			if (!menu.getStoreId().equals(store.getStoreId())) {
				throw new BusinessException(ErrorCode.ORDER_MENU_STORE_MISMATCH);
			}

			if (menu.getMenuStatus() == MenuStatus.HIDDEN) {
				throw new BusinessException(ErrorCode.MENU_HIDDEN);
			}

			// 가격 계산
			int price = menu.getPrice() * menuDto.getQuantity();

			totalPrice += price;

		}

		// 주문 저장
		Order order = new Order(
			userId,
			store.getStoreId(),
			request.getAddress(),
			request.getDetailAddress(),
			totalPrice
		);

		orderRepository.save(order);

		// 주문 상품 생성
		List<OrderItem> orderItems = new ArrayList<>();

		for (ReqCreateOrderMenuDto menuDto : request.getMenuList()) {

			Menu menu = menuRepository.findById(menuDto.getMenuId())
				.orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

			OrderItem orderItem = new OrderItem(
				order.getOrderId(),
				menu.getMenuId(),
				menuDto.getQuantity(),
				menu.getPrice()
			);

			orderItems.add(orderItem);
		}

		orderItemRepository.saveAll(orderItems);

		// 응답 반환
		return new ResCreateOrderDto(order.getOrderId());

		// TODO : Menu 2회 조회 -> 1회 조회로 최적화
		// TODO : 메서드 분리
	}


	@Transactional(readOnly = true)
	public ResOrderDto getOrder(UUID orderId) {

		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

		Store store = storeRepository.findById(order.getStoreId())
			.orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

		return ResOrderDto.builder()
			.orderId(order.getOrderId())
			.storeName(store.getName())
			.address(order.getAddress())
			.totalPrice(order.getTotalPrice())
			.status(order.getStatus())
			.orderedAt(order.getCreatedAt())
			.build();
	}

	// TODO : 고객은 자신의 주문만 조회 가능 -> 검증 메서드 들어가야 함


	@Transactional(readOnly = true)
	public ResOrderItemsDto getOrderItems(UUID orderId) {

		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

		List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(order.getOrderId());

		List<ResOrderItemDto> items = orderItems.stream()
			.map(orderItem -> {

				Menu menu = menuRepository.findById(orderItem.getMenuId())
					.orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

				return ResOrderItemDto.builder()
					.menuId(orderItem.getMenuId())
					.menuName(menu.getMenuName())
					.quantity(orderItem.getQuantity())
					.price(orderItem.getPrice())
					.build();
			})
			.toList();

		return ResOrderItemsDto.builder()
			.orderId(order.getOrderId())
			.items(items)
			.build();

		// TODO : Menu N+1 조회 -> findAllById 배치 조회로 최적화 or QueryDSL
		// TODO : 고객은 자신의 주문만 조회 가능 -> 권한 검증 추가
	}


	@Transactional(readOnly = true)
	public ResOrderPageDto getOrders(String status, Pageable pageable) {

		Page<Order> orderPage;

		// status == ALL 이면 전체 조회
		if ("ALL".equalsIgnoreCase(status)) {
			orderPage = orderRepository.findAll(pageable);
		} else {

			OrderStatus orderStatus;

			try {
				orderStatus = OrderStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
			}

			orderPage = orderRepository.findAllByStatus(orderStatus, pageable);
		}

		List<ResOrderListDto> orders = orderPage.getContent().stream()
			.map(order -> {

				Store store = storeRepository.findById(order.getStoreId())
					.orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

				return ResOrderListDto.builder()
					.orderId(order.getOrderId())
					.storeName(store.getName())
					.totalPrice(order.getTotalPrice())
					.status(order.getStatus())
					.orderedAt(order.getCreatedAt())
					.build();
			})
			.toList();

		return ResOrderPageDto.builder()
			.orders(orders)
			.page(orderPage.getNumber())
			.size(orderPage.getSize())
			.totalElements(orderPage.getTotalElements())
			.totalPages(orderPage.getTotalPages())
			.build();
	}

	// TODO QueryDSL 적용 후 startDate/endDate 조건 검색 추가
	// TODO Authentication 연동 후 권한별(고객/사장/관리자) 조회 추가


	@Transactional
	public ResOrderStatusDto changeStatus(UUID orderId, OrderStatus status) {

		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

		// 취소(CANCELED)는 5분 제한 등 별도 규칙이 있는 '주문 취소' API로만 처리한다.
		if (status == OrderStatus.CANCELED) {
			throw new BusinessException(ErrorCode.ORDER_STATUS_TRANSITION_INVALID);
		}

		// 상태 전이 유효성은 Order 엔티티에서 검증 (변경분은 dirty checking으로 반영)
		order.changeStatus(status);

		return ResOrderStatusDto.builder()
			.orderId(order.getOrderId())
			.status(order.getStatus())
			.build();

		// TODO : 사장/관리자 권한 검증 추가 (본인 가게 주문인지 등)
	}


	@Transactional
	public ResOrderStatusDto cancelOrder(Long userId, UUID orderId) {

		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

		// 고객은 본인 주문만 취소 가능
		if (!order.getUserId().equals(userId)) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		// 5분 제한 및 상태 전이 검증은 Order 엔티티의 cancel()에서 처리
		order.cancel();

		return ResOrderStatusDto.builder()
			.orderId(order.getOrderId())
			.status(order.getStatus())
			.build();
	}
}
