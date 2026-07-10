package com.example.delivery.order.dto.request;

import java.time.LocalDate;

import com.example.delivery.order.entity.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "주문 검색 조건 DTO (QueryDSL 적용 예정)")
public class ReqOrderSearchDto {

	@Schema(description = "주문 상태 필터", example = "COMPLETED")
	private OrderStatus status;

	@Schema(description = "조회 시작일", example = "2026-07-01")
	private LocalDate startDate;

	@Schema(description = "조회 종료일", example = "2026-07-09")
	private LocalDate endDate;

}
