package com.example.delivery.order.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReqCreateOrderMenuDto {

	@NotNull
	private UUID menuId;

	@NotNull
	@Min(1)
	private Integer quantity;
}