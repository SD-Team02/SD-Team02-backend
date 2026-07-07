package com.example.delivery.order.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, UUID> {

	Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

}
