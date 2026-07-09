package com.example.delivery.order.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, UUID> {

	// 단건 (soft delete 제외)
	Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);

	// MANAGER/MASTER - 전체 조회
	Page<Order> findByDeletedAtIsNull(Pageable pageable);

	Page<Order> findByStatusAndDeletedAtIsNull(OrderStatus status, Pageable pageable);

	// CUSTOMER - 본인 주문만 조회
	Page<Order> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

	Page<Order> findByUserIdAndStatusAndDeletedAtIsNull(Long userId, OrderStatus status, Pageable pageable);

	// OWNER - 본인 가게 주문만 조회
	Page<Order> findByStoreIdInAndDeletedAtIsNull(List<UUID> storeIds, Pageable pageable);

	Page<Order> findByStoreIdInAndStatusAndDeletedAtIsNull(List<UUID> storeIds, OrderStatus status, Pageable pageable);
}
