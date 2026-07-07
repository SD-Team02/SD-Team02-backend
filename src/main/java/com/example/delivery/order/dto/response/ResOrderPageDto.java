package com.example.delivery.order.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResOrderPageDto {

	private List<ResOrderListDto> orders;

	private int page;

	private int size;

	private long totalElements;

	private int totalPages;
}
