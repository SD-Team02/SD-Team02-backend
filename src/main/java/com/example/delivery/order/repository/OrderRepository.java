package com.example.delivery.order.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.delivery.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {


}
