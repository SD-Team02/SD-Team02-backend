package com.example.delivery.order.dto.response;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "주문 상품 목록 응답 DTO")
public class ResOrderItemsDto {

	@Schema(description = "주문 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID orderId;

	@Schema(description = "주문 상품 목록")
	private List<ResOrderItemDto> items;
}
