package com.example.delivery.order.dto.request;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "주문 생성 요청 DTO")
public class ReqCreateOrderDto {

	@NotNull
	@Schema(description = "주문할 가게 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private UUID storeId;

	@NotBlank
	@Size(max = 255)
	@Schema(description = "배송 주소 (최대 255자)", example = "서울시 강남구 테헤란로 123")
	private String address;

	@NotBlank
	@Size(max = 255)
	@Schema(description = "상세 주소 (최대 255자)", example = "101동 1001호")
	private String detailAddress;

	@Valid
	@NotEmpty
	@Schema(description = "주문 메뉴 목록")
	private List<ReqCreateOrderMenuDto> menuList;
}
