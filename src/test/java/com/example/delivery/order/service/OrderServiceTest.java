package com.example.delivery.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.repository.MenuRepository;
import com.example.delivery.order.dto.request.ReqCreateOrderDto;
import com.example.delivery.order.dto.request.ReqCreateOrderMenuDto;
import com.example.delivery.order.dto.response.ResCreateOrderDto;
import com.example.delivery.order.dto.response.ResOrderDto;
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

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderItemRepository orderItemRepository;

	@Mock
	private MenuRepository menuRepository;

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private OrderService orderService;

	private final Pageable pageable = PageRequest.of(0, 10);

	private static final Logger log = LoggerFactory.getLogger(OrderServiceTest.class);

	@BeforeEach
	void printTestName(TestInfo testInfo) {
		log.info("");
		log.info("========================================================");
		log.info("▶ 실행 테스트: {}", testInfo.getDisplayName());
		log.info("========================================================");
	}

	// ===== 헬퍼 =====

	private Store createStore(UUID storeId, Long ownerId) {
		Store store = new Store(ownerId, UUID.randomUUID(), UUID.randomUUID(),
			"테스트가게", "가게주소", "01000000000", LocalTime.of(9, 0), LocalTime.of(22, 0));
		ReflectionTestUtils.setField(store, "storeId", storeId);
		return store;
	}

	private Order createOrderEntity(UUID orderId, Long userId, UUID storeId) {
		Order order = new Order(userId, storeId, "주소", "상세주소", 20000);
		ReflectionTestUtils.setField(order, "orderId", orderId);
		return order;
	}

	private Menu createMenu(UUID menuId, UUID storeId, int price) {
		Menu menu = new Menu(storeId, "후라이드 치킨", price, "설명", false);
		ReflectionTestUtils.setField(menu, "menuId", menuId);
		return menu;
	}

	private ReqCreateOrderDto createReq(UUID storeId, UUID menuId, int quantity) {
		ReqCreateOrderMenuDto menuDto = new ReqCreateOrderMenuDto();
		ReflectionTestUtils.setField(menuDto, "menuId", menuId);
		ReflectionTestUtils.setField(menuDto, "quantity", quantity);

		ReqCreateOrderDto req = new ReqCreateOrderDto();
		ReflectionTestUtils.setField(req, "storeId", storeId);
		ReflectionTestUtils.setField(req, "address", "주소");
		ReflectionTestUtils.setField(req, "detailAddress", "상세주소");
		ReflectionTestUtils.setField(req, "menuList", List.of(menuDto));
		return req;
	}

	// ===== createOrder =====

	@Test
	@DisplayName("주문 생성 - 성공 (총액 = 단가 * 수량)")
	void createOrder_success() {
		// given
		UUID storeId = UUID.randomUUID();
		UUID menuId = UUID.randomUUID();
		Store store = createStore(storeId, 1L);
		Menu menu = createMenu(menuId, storeId, 10000);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

		// when
		ResCreateOrderDto result = orderService.createOrder(1L, createReq(storeId, menuId, 2));

		// then
		assertThat(result).isNotNull();

		ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
		verify(orderRepository).save(captor.capture());
		log.info("저장된 주문 총액={}, 상태={}", captor.getValue().getTotalPrice(), captor.getValue().getStatus());
		assertThat(captor.getValue().getTotalPrice()).isEqualTo(20000);
		assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.REQUESTED);
		verify(orderItemRepository).saveAll(any());
	}

	@Test
	@DisplayName("주문 생성 - 가게 없음")
	void createOrder_storeNotFound() {
		UUID storeId = UUID.randomUUID();
		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.createOrder(1L, createReq(storeId, UUID.randomUUID(), 1)))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.STORE_NOT_FOUND);
	}

	@Test
	@DisplayName("주문 생성 - 메뉴 없음")
	void createOrder_menuNotFound() {
		UUID storeId = UUID.randomUUID();
		UUID menuId = UUID.randomUUID();
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(createStore(storeId, 1L)));
		when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.createOrder(1L, createReq(storeId, menuId, 1)))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.MENU_NOT_FOUND);
	}

	@Test
	@DisplayName("주문 생성 - 다른 가게 메뉴")
	void createOrder_menuStoreMismatch() {
		UUID storeId = UUID.randomUUID();
		UUID menuId = UUID.randomUUID();
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(createStore(storeId, 1L)));
		// 메뉴는 다른 가게 소속
		when(menuRepository.findById(menuId)).thenReturn(Optional.of(createMenu(menuId, UUID.randomUUID(), 10000)));

		assertThatThrownBy(() -> orderService.createOrder(1L, createReq(storeId, menuId, 1)))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ORDER_MENU_STORE_MISMATCH);
	}

	@Test
	@DisplayName("주문 생성 - 숨김 메뉴")
	void createOrder_menuHidden() {
		UUID storeId = UUID.randomUUID();
		UUID menuId = UUID.randomUUID();
		Menu menu = createMenu(menuId, storeId, 10000);
		menu.hide();
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(createStore(storeId, 1L)));
		when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

		assertThatThrownBy(() -> orderService.createOrder(1L, createReq(storeId, menuId, 1)))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.MENU_HIDDEN);
	}

	// ===== getOrders (역할별 조회 범위) =====

	@Test
	@DisplayName("주문 목록 - CUSTOMER는 본인 주문만 조회")
	void getOrders_customer() {
		UUID storeId = UUID.randomUUID();
		Order order = createOrderEntity(UUID.randomUUID(), 1L, storeId);
		Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);

		when(orderRepository.findByUserIdAndDeletedAtIsNull(1L, pageable)).thenReturn(page);
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(createStore(storeId, 9L)));

		PageResponse<ResOrderListDto> result = orderService.getOrders(1L, Role.CUSTOMER, "ALL", pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(orderRepository).findByUserIdAndDeletedAtIsNull(1L, pageable);
	}

	@Test
	@DisplayName("주문 목록 - OWNER는 본인 가게 주문만 조회")
	void getOrders_owner() {
		UUID storeId = UUID.randomUUID();
		Store store = createStore(storeId, 1L);
		Order order = createOrderEntity(UUID.randomUUID(), 5L, storeId);
		Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);

		when(storeRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(List.of(store));
		when(orderRepository.findByStoreIdInAndDeletedAtIsNull(List.of(storeId), pageable)).thenReturn(page);
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

		PageResponse<ResOrderListDto> result = orderService.getOrders(1L, Role.OWNER, "ALL", pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(orderRepository).findByStoreIdInAndDeletedAtIsNull(List.of(storeId), pageable);
	}

	@Test
	@DisplayName("주문 목록 - OWNER인데 소유 가게가 없으면 빈 페이지")
	void getOrders_owner_noStores() {
		when(storeRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(List.of());

		PageResponse<ResOrderListDto> result = orderService.getOrders(1L, Role.OWNER, "ALL", pageable);

		assertThat(result.getContent()).isEmpty();
		verify(orderRepository, never()).findByStoreIdInAndDeletedAtIsNull(any(), any());
	}

	@Test
	@DisplayName("주문 목록 - MANAGER는 전체 조회")
	void getOrders_manager() {
		UUID storeId = UUID.randomUUID();
		Order order = createOrderEntity(UUID.randomUUID(), 5L, storeId);
		Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);

		when(orderRepository.findByDeletedAtIsNull(pageable)).thenReturn(page);
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(createStore(storeId, 9L)));

		PageResponse<ResOrderListDto> result = orderService.getOrders(1L, Role.MANAGER, "ALL", pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(orderRepository).findByDeletedAtIsNull(pageable);
	}

	@Test
	@DisplayName("주문 목록 - 잘못된 상태값")
	void getOrders_invalidStatus() {
		assertThatThrownBy(() -> orderService.getOrders(1L, Role.MASTER, "WRONG", pageable))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.INVALID_ORDER_STATUS);
	}

	// ===== getOrder (단건 소유권) =====

	@Test
	@DisplayName("주문 단건 - MANAGER 성공")
	void getOrder_manager_success() {
		UUID orderId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId))
			.thenReturn(Optional.of(createOrderEntity(orderId, 5L, storeId)));
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(createStore(storeId, 9L)));

		ResOrderDto result = orderService.getOrder(1L, Role.MANAGER, orderId);

		assertThat(result.getOrderId()).isEqualTo(orderId);
		assertThat(result.getStoreName()).isEqualTo("테스트가게");
	}

	@Test
	@DisplayName("주문 단건 - 없음")
	void getOrder_notFound() {
		UUID orderId = UUID.randomUUID();
		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.getOrder(1L, Role.MANAGER, orderId))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ORDER_NOT_FOUND);
	}

	@Test
	@DisplayName("주문 단건 - CUSTOMER 본인 주문 아니면 거부")
	void getOrder_customer_notOwner() {
		UUID orderId = UUID.randomUUID();
		// 주문 주인은 2L, 요청자는 1L
		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId))
			.thenReturn(Optional.of(createOrderEntity(orderId, 2L, UUID.randomUUID())));

		assertThatThrownBy(() -> orderService.getOrder(1L, Role.CUSTOMER, orderId))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("주문 단건 - OWNER 본인 가게 아니면 거부")
	void getOrder_owner_otherStore() {
		UUID orderId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId))
			.thenReturn(Optional.of(createOrderEntity(orderId, 5L, storeId)));
		// 가게 주인은 2L, 요청 OWNER는 1L
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(createStore(storeId, 2L)));

		assertThatThrownBy(() -> orderService.getOrder(1L, Role.OWNER, orderId))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	// ===== getOrderItems =====

	@Test
	@DisplayName("주문 상품 목록 - 성공")
	void getOrderItems_success() {
		UUID orderId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		UUID menuId = UUID.randomUUID();
		OrderItem item = new OrderItem(orderId, menuId, 2, 10000);

		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId))
			.thenReturn(Optional.of(createOrderEntity(orderId, 5L, storeId)));
		when(orderItemRepository.findAllByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(List.of(item));
		when(menuRepository.findById(menuId)).thenReturn(Optional.of(createMenu(menuId, storeId, 10000)));

		ResOrderItemsDto result = orderService.getOrderItems(1L, Role.MANAGER, orderId);

		assertThat(result.getItems()).hasSize(1);
		assertThat(result.getItems().get(0).getMenuName()).isEqualTo("후라이드 치킨");
		assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
	}

	@Test
	@DisplayName("주문 상품 목록 - CUSTOMER 본인 아니면 거부")
	void getOrderItems_customer_notOwner() {
		UUID orderId = UUID.randomUUID();
		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId))
			.thenReturn(Optional.of(createOrderEntity(orderId, 2L, UUID.randomUUID())));

		assertThatThrownBy(() -> orderService.getOrderItems(1L, Role.CUSTOMER, orderId))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	// ===== changeStatus =====

	@Test
	@DisplayName("주문 상태 변경 - 성공 (REQUESTED -> ACCEPTED)")
	void changeStatus_success() {
		UUID orderId = UUID.randomUUID();
		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId))
			.thenReturn(Optional.of(createOrderEntity(orderId, 5L, UUID.randomUUID())));

		ResOrderStatusDto result = orderService.changeStatus(1L, Role.MANAGER, orderId, OrderStatus.ACCEPTED);

		assertThat(result.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
	}

	@Test
	@DisplayName("주문 상태 변경 - CANCELED는 상태변경 API로 불가")
	void changeStatus_canceledBlocked() {
		UUID orderId = UUID.randomUUID();
		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId))
			.thenReturn(Optional.of(createOrderEntity(orderId, 5L, UUID.randomUUID())));

		assertThatThrownBy(() -> orderService.changeStatus(1L, Role.MANAGER, orderId, OrderStatus.CANCELED))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ORDER_STATUS_TRANSITION_INVALID);
	}

	@Test
	@DisplayName("주문 상태 변경 - 불가능한 전이 (REQUESTED -> DELIVERED)")
	void changeStatus_invalidTransition() {
		UUID orderId = UUID.randomUUID();
		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId))
			.thenReturn(Optional.of(createOrderEntity(orderId, 5L, UUID.randomUUID())));

		assertThatThrownBy(() -> orderService.changeStatus(1L, Role.MANAGER, orderId, OrderStatus.DELIVERED))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ORDER_STATUS_TRANSITION_INVALID);
	}

	@Test
	@DisplayName("주문 상태 변경 - OWNER 본인 가게 아니면 거부")
	void changeStatus_owner_otherStore() {
		UUID orderId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId))
			.thenReturn(Optional.of(createOrderEntity(orderId, 5L, storeId)));
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(createStore(storeId, 2L)));

		assertThatThrownBy(() -> orderService.changeStatus(1L, Role.OWNER, orderId, OrderStatus.ACCEPTED))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	// ===== cancelOrder =====

	@Test
	@DisplayName("주문 취소 - 성공 (본인, 5분 이내)")
	void cancelOrder_success() {
		UUID orderId = UUID.randomUUID();
		Order order = createOrderEntity(orderId, 1L, UUID.randomUUID());
		ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now());
		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(Optional.of(order));

		ResOrderStatusDto result = orderService.cancelOrder(1L, orderId);

		assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELED);
	}

	@Test
	@DisplayName("주문 취소 - 본인 주문 아니면 거부")
	void cancelOrder_notOwner() {
		UUID orderId = UUID.randomUUID();
		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId))
			.thenReturn(Optional.of(createOrderEntity(orderId, 2L, UUID.randomUUID())));

		assertThatThrownBy(() -> orderService.cancelOrder(1L, orderId))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
	}

	@Test
	@DisplayName("주문 취소 - 5분 초과")
	void cancelOrder_timeExceeded() {
		UUID orderId = UUID.randomUUID();
		Order order = createOrderEntity(orderId, 1L, UUID.randomUUID());
		ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now().minusMinutes(6));
		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(Optional.of(order));

		assertThatThrownBy(() -> orderService.cancelOrder(1L, orderId))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ORDER_CANCEL_TIME_EXCEEDED);
	}

	// ===== deleteOrder =====

	@Test
	@DisplayName("주문 삭제 - soft delete 처리")
	void deleteOrder_success() {
		UUID orderId = UUID.randomUUID();
		Order order = createOrderEntity(orderId, 5L, UUID.randomUUID());
		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(Optional.of(order));

		orderService.deleteOrder(1L, orderId);

		assertThat(order.isDeleted()).isTrue();
		verify(orderRepository, never()).delete(any());
	}

	@Test
	@DisplayName("주문 삭제 - 없음")
	void deleteOrder_notFound() {
		UUID orderId = UUID.randomUUID();
		when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.deleteOrder(1L, orderId))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.ORDER_NOT_FOUND);
	}
}
