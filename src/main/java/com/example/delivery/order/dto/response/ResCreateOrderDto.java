package com.example.delivery.order.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResCreateOrderDto {

	private UUID orderId;
}