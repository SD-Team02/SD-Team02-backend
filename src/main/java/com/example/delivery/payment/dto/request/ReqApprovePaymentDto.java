package com.example.delivery.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReqApprovePaymentDto {

    @NotNull(message = "주문 ID는 필수입니다.")
    private UUID orderId;

    @NotNull(message = "결제 금액은 필수입니다.")
    @Min(value = 0, message = "결제 금액은 0원 이상이어야 합니다.")
    private Integer amount;

    @NotBlank(message = "결제 수단은 필수입니다.")
    private String paymentMethod;

    @NotBlank(message = "카드 회사명은 필수입니다.")
    private String cardCompany;
}