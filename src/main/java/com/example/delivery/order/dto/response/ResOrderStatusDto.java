package com.example.delivery.order.dto.response;

import java.util.UUID;

import com.example.delivery.order.entity.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "주문 상태 응답 DTO (상태 변경/취소 공통)")
public class ResOrderStatusDto {

	@Schema(description = "주문 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID orderId;

	@Schema(description = "현재 주문 상태", example = "COOKING")
	private OrderStatus status;
}
