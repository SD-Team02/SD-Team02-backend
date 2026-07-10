package com.example.delivery.order.dto.response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "주문 상품 항목 응답 DTO")
public class ResOrderItemDto {

	@Schema(description = "메뉴 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID menuId;

	@Schema(description = "메뉴 이름", example = "후라이드 치킨")
	private String menuName;

	@Schema(description = "주문 수량", example = "2")
	private Integer quantity;

	@Schema(description = "주문 시점 단가(스냅샷)", example = "18000")
	private Integer price;
}
