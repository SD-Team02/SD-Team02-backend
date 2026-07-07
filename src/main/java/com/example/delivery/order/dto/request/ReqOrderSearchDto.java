package com.example.delivery.order.dto.request;

import java.time.LocalDate;

import com.example.delivery.order.entity.OrderStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqOrderSearchDto {

	private OrderStatus status;

	private LocalDate startDate;

	private LocalDate endDate;

}
