package com.example.delivery.payment.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.common.util.PageableFactory;
import com.example.delivery.payment.dto.request.ReqApprovePaymentDto;
import com.example.delivery.payment.dto.request.ReqPaymentSearchDto;
import com.example.delivery.payment.dto.response.ResApprovePaymentDto;
import com.example.delivery.payment.dto.response.ResPaymentDto;
import com.example.delivery.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    // 2. 결제 내역 단건 조회
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<ResPaymentDto>> getPayment(
            @PathVariable("paymentId") UUID paymentId
    ) {
        ResPaymentDto response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponse.success("결제 내역 조회 성공", response));
    }

    /** 3. 고객 본인용: 기간 범위별 목록 조회
     * JWT인증 생기면 확인해야함
     * */
    @GetMapping("/customer")
    public ResponseEntity<ApiResponse<PageResponse<ResPaymentDto>>> getMyPayments(
            @Valid @ModelAttribute ReqPaymentSearchDto searchDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();

        // 중복 기본값 제거 피드백 반영: 주소창 파라미터가 비어있어도 PageableFactory 내부에서 완벽히 기본값을 입혀줍니다!
        Pageable pageable = PageableFactory.of(
                searchDto.getPage(),
                searchDto.getSize(),
                searchDto.getSortBy(),
                searchDto.getDirection()
        );

        Page<ResPaymentDto> pageData = paymentService.getMyPaymentsByPeriod(userId, searchDto, pageable);

        //공통 페이징 호출
        PageResponse<ResPaymentDto> pageResponse = PageResponse.from(pageData);
        return ResponseEntity.ok(ApiResponse.success("본인 결제 내역 목록 조회 성공", pageResponse));
    }

}
