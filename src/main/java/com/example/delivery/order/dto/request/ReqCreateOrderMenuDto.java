package com.example.delivery.order.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "주문 메뉴 항목 요청 DTO")
public class ReqCreateOrderMenuDto {

	@NotNull
	@Schema(description = "메뉴 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID menuId;

	@NotNull
	@Min(1)
	@Schema(description = "주문 수량 (1개 이상)", example = "2")
	private Integer quantity;
}
