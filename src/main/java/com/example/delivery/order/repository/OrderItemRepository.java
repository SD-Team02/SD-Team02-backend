package com.example.delivery.order.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.delivery.order.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}