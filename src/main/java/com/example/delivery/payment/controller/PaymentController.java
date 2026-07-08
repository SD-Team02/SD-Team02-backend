package com.example.delivery.payment.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.payment.dto.request.ReqApprovePaymentDto;
import com.example.delivery.payment.dto.response.ResApprovePaymentDto;
import com.example.delivery.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 1. 가상 결제 승인 API
     * JWT인증 생기면 확인해야함
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ResApprovePaymentDto>> approvePayment(
            @Valid @RequestBody ReqApprovePaymentDto requestDto
            , @AuthenticationPrincipal UserDetailsImpl userDetails // 시큐리티 유저객체 (수정가능성 있음)
    ) {
        // 로그인 회원 Long 고유 ID 추출
        Long userId = userDetails.getUserId();

        ResApprovePaymentDto response = paymentService.approve(requestDto, userId);
        return ResponseEntity.ok(ApiResponse.success("결제 등록 성공", response));
    }

}
