package com.example.delivery.order.service;

import java.util.ArrayList;
import java.util.List;

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
import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderItem;
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


}
