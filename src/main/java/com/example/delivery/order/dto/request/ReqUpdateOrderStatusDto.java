package com.example.delivery.order.dto.request;

import com.example.delivery.order.entity.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "주문 상태 변경 요청 DTO")
public class ReqUpdateOrderStatusDto {

	@NotNull
	@Schema(description = "변경할 주문 상태", example = "COOKING")
	private OrderStatus status;
}
