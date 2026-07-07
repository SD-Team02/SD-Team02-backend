package com.example.delivery.order.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.delivery.order.entity.OrderStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResOrderDto {

	private UUID orderId;
	private String storeName;
	private String address;
	private Integer totalPrice;
	private OrderStatus status;
	private LocalDateTime orderedAt;

}