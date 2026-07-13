package com.example.delivery.order.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.delivery.order.entity.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "주문 단건 조회 응답 DTO")
public class ResOrderDto {

	@Schema(description = "주문 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID orderId;

	@Schema(description = "가게 이름", example = "맛있는 치킨집")
	private String storeName;

	@Schema(description = "배송 주소", example = "서울시 강남구 테헤란로 123 101동 1001호")
	private String address;

	@Schema(description = "총 주문 금액", example = "25000")
	private Integer totalPrice;

	@Schema(description = "주문 상태", example = "COMPLETED")
	private OrderStatus status;

	@Schema(description = "주문 일시", example = "2026-07-09T12:34:56")
	private LocalDateTime orderedAt;

}
