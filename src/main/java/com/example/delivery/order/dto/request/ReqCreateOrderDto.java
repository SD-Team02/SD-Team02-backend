package com.example.delivery.order.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReqCreateOrderDto {

	@NotNull
	private UUID storeId;

	@NotBlank
	private String address;

	@NotBlank
	private String detailAddress;

	@Valid
	@NotEmpty
	private List<ReqCreateOrderMenuDto> menuList;
}