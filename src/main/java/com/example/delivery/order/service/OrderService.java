package com.example.delivery.order.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.delivery.global.common.response.PageResponse;
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
import com.example.delivery.order.dto.response.ResOrderStatusDto;
import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderItem;
import com.example.delivery.order.entity.OrderStatus;
import com.example.delivery.order.repository.OrderItemRepository;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.store.entity.Store;
import com.example.delivery.store.repository.StoreRepository;
import com.example.delivery.user.entity.Role;

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

		// 요청 메뉴를 한 번에 조회해 Map으로 재사용 (검증 루프 + 주문상품 생성 루프 공통 사용, N+1 방지)
		List<UUID> menuIds = request.getMenuList().stream()
			.map(ReqCreateOrderMenuDto::getMenuId)
			.toList();

		Map<UUID, Menu> menuMap = menuRepository.findAllById(menuIds).stream()
			.collect(Collectors.toMap(Menu::getMenuId, Function.identity()));

		// 총 금액 계산 및 메뉴 검증
		int totalPrice = 0;

		for (ReqCreateOrderMenuDto menuDto : request.getMenuList()) {

			Menu menu = menuMap.get(menuDto.getMenuId());
			if (menu == null) {
				throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
			}

			// 다른 가게 메뉴인지 검증
			if (!menu.getStoreId().equals(store.getStoreId())) {
				throw new BusinessException(ErrorCode.ORDER_MENU_STORE_MISMATCH);
			}

			if (menu.getMenuStatus() == MenuStatus.HIDDEN) {
				throw new BusinessException(ErrorCode.MENU_HIDDEN);
			}

			// 가격 계산
			totalPrice += menu.getPrice() * menuDto.getQuantity();
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

		// 주문 상품 생성 (위에서 검증된 menuMap 재사용)
		List<OrderItem> orderItems = request.getMenuList().stream()
			.map(menuDto -> {
				Menu menu = menuMap.get(menuDto.getMenuId());
				return new OrderItem(
					order.getOrderId(),
					menu.getMenuId(),
					menuDto.getQuantity(),
					menu.getPrice()
				);
			})
			.toList();

		orderItemRepository.saveAll(orderItems);

		// 응답 반환
		return new ResCreateOrderDto(order.getOrderId());
	}


	@Transactional(readOnly = true)
	public ResOrderDto getOrder(Long userId, Role role, UUID orderId) {

		Order order = orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

		// 조회 권한 검증 (CUSTOMER: 본인 / OWNER: 본인 가게 / MANAGER·MASTER: 전체)
		validateOrderAccess(order, userId, role);

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


	@Transactional(readOnly = true)
	public ResOrderItemsDto getOrderItems(Long userId, Role role, UUID orderId) {

		Order order = orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

		// 조회 권한 검증 (CUSTOMER: 본인 / OWNER: 본인 가게 / MANAGER·MASTER: 전체)
		validateOrderAccess(order, userId, role);

		List<OrderItem> orderItems = orderItemRepository.findAllByOrderIdAndDeletedAtIsNull(order.getOrderId());

		// 주문상품의 메뉴를 한 번에 조회해 Map으로 재사용 (N+1 방지)
		List<UUID> menuIds = orderItems.stream()
			.map(OrderItem::getMenuId)
			.toList();

		Map<UUID, Menu> menuMap = menuRepository.findAllById(menuIds).stream()
			.collect(Collectors.toMap(Menu::getMenuId, Function.identity()));

		List<ResOrderItemDto> items = orderItems.stream()
			.map(orderItem -> {

				Menu menu = menuMap.get(orderItem.getMenuId());
				if (menu == null) {
					throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
				}

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
	}


	@Transactional(readOnly = true)
	// orderStatus == null 이면 상태 무관(전체), 그 외엔 해당 상태만 조회
	public PageResponse<ResOrderListDto> getOrders(Long userId, Role role, OrderStatus orderStatus, Pageable pageable) {

		// 역할별 조회 범위 (soft delete 제외)
		Page<Order> orderPage = switch (role) {
			// CUSTOMER : 본인 주문만
			case CUSTOMER -> (orderStatus == null)
				? orderRepository.findByUserIdAndDeletedAtIsNull(userId, pageable)
				: orderRepository.findByUserIdAndStatusAndDeletedAtIsNull(userId, orderStatus, pageable);

			// OWNER : 본인 가게 주문만
			case OWNER -> {
				List<UUID> storeIds = storeRepository.findByUserIdAndDeletedAtIsNull(userId).stream()
					.map(Store::getStoreId)
					.toList();

				if (storeIds.isEmpty()) {
					yield Page.empty(pageable);
				}

				yield (orderStatus == null)
					? orderRepository.findByStoreIdInAndDeletedAtIsNull(storeIds, pageable)
					: orderRepository.findByStoreIdInAndStatusAndDeletedAtIsNull(storeIds, orderStatus, pageable);
			}

			// MANAGER, MASTER : 전체
			case MANAGER, MASTER -> (orderStatus == null)
				? orderRepository.findByDeletedAtIsNull(pageable)
				: orderRepository.findByStatusAndDeletedAtIsNull(orderStatus, pageable);
		};

		// 페이지 내 주문들의 가게를 한 번에 조회해 Map으로 재사용 (건별 조회 N+1 방지)
		List<UUID> storeIds = orderPage.getContent().stream()
			.map(Order::getStoreId)
			.distinct()
			.toList();

		Map<UUID, Store> storeMap = storeRepository.findAllById(storeIds).stream()
			.collect(Collectors.toMap(Store::getStoreId, Function.identity()));

		Page<ResOrderListDto> responsePage = orderPage.map(order -> {

			Store store = storeMap.get(order.getStoreId());
			if (store == null) {
				throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
			}

			return ResOrderListDto.builder()
				.orderId(order.getOrderId())
				.storeName(store.getName())
				.totalPrice(order.getTotalPrice())
				.status(order.getStatus())
				.orderedAt(order.getCreatedAt())
				.build();
		});

		return PageResponse.from(responsePage);

		// TODO QueryDSL 적용 후 startDate/endDate 조건 검색 추가
	}

	@Transactional
	public ResOrderStatusDto changeStatus(Long userId, Role role, UUID orderId, OrderStatus status) {

		Order order = orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

		// OWNER는 본인 가게 주문만, MANAGER·MASTER는 전체 변경 가능
		validateOrderAccess(order, userId, role);

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
	}


	@Transactional
	public ResOrderStatusDto cancelOrder(Long userId, UUID orderId) {

		Order order = orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)
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


	@Transactional
	public void deleteOrder(Long userId, UUID orderId) {

		Order order = orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

		// 실제 삭제가 아닌 soft delete (deletedAt/deletedBy 기록)
		order.softDelete(userId);

		// TODO : 관리자(MANAGER/MASTER) 권한 검증 추가
	}

	// 접근 권한: CUSTOMER=본인 주문, OWNER=본인 가게 주문, MANAGER·MASTER=전체
	private void validateOrderAccess(Order order, Long userId, Role role) {
		switch (role) {
			case CUSTOMER -> {
				if (!order.getUserId().equals(userId)) {
					throw new BusinessException(ErrorCode.ACCESS_DENIED);
				}
			}
			case OWNER -> {
				Store store = storeRepository.findById(order.getStoreId())
					.orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
				if (!store.getUserId().equals(userId)) {
					throw new BusinessException(ErrorCode.ACCESS_DENIED);
				}
			}
			case MANAGER, MASTER -> {
				// 전체 접근 허용
			}
		}
	}
}
