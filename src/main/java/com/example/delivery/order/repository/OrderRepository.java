package com.example.delivery.order.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, UUID> {

	// soft delete 되지 않은 주문만 조회 (deletedAt is null)
	Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);

	Page<Order> findByDeletedAtIsNull(Pageable pageable);

	Page<Order> findByStatusAndDeletedAtIsNull(OrderStatus status, Pageable pageable);
}
