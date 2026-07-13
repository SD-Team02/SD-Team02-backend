package com.example.delivery.order.dto.response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "주문 생성 응답 DTO")
public class ResCreateOrderDto {

	@Schema(description = "생성된 주문 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID orderId;
}
