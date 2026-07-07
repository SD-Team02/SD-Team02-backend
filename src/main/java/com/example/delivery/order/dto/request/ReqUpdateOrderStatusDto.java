package com.example.delivery.order.dto.request;

import com.example.delivery.order.entity.OrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReqUpdateOrderStatusDto {

	@NotNull
	private OrderStatus status;
}
