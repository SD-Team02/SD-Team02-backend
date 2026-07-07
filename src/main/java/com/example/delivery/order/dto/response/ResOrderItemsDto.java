package com.example.delivery.order.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResOrderItemsDto {

	private UUID orderId;

	private List<ResOrderItemDto> items;
}
