package com.example.delivery.payment.dto.request;

import com.example.delivery.payment.entity.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter // @ModelAttribute 조율용 바인딩 세터 유지
@NoArgsConstructor
public class ReqPaymentSearchDto {

    private LocalDate startDate;

    private LocalDate endDate;

    private PaymentStatus status;

}