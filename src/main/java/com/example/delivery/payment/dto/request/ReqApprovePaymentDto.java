package com.example.delivery.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@Schema(description = "결제 승인 요청 정보")
public class ReqApprovePaymentDto {

    @NotNull(message = "주문 ID는 필수입니다.")
    @Schema(description = "주문 고유 ID", example = "a1b2c3d4-5678-90ab-cdef-1234567890ab")
    private UUID orderId;

    @NotNull(message = "결제 금액은 필수입니다.")
    @Min(value = 0, message = "결제 금액은 0원 이상이어야 합니다.")
    @Schema(description = "최종 결제 금액 (원 단위)", example = "25000")
    private Integer amount;

    @NotBlank(message = "결제 수단은 필수입니다.")
    @Schema(description = "결제 수단", example = "CARD")
    private String paymentMethod;

    @NotBlank(message = "카드 회사명은 필수입니다.")
    @Schema(description = "카드 회사", example = "신한카드")
    private String cardCompany;
}