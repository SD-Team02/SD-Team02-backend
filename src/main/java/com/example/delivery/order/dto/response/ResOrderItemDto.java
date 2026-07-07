package com.example.delivery.order.dto.response;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResOrderItemDto {

	private UUID menuId;

	private String menuName;

	private Integer quantity;

	private Integer price;
}
