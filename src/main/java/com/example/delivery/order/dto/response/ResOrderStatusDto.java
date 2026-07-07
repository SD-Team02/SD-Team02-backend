package com.example.delivery.order.dto.response;

import java.util.UUID;

import com.example.delivery.order.entity.OrderStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResOrderStatusDto {

	private UUID orderId;

	private OrderStatus status;
}
