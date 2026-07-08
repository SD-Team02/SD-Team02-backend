package com.example.delivery.payment.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.common.util.PageableFactory;
import com.example.delivery.payment.dto.request.ReqApprovePaymentDto;
import com.example.delivery.payment.dto.request.ReqPaymentSearchDto;
import com.example.delivery.payment.dto.response.ResApprovePaymentDto;
import com.example.delivery.payment.dto.response.ResPaymentDto;
import com.example.delivery.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "결제 승인 등록", description = "주문 정보를 기반으로 가상 결제 승인을 진행합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "결제 요청 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 주문 정보입니다.")
    })
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
    @Operation(summary = "결제 단건 상세 조회", description = "결제 ID를 통해 특정 결제 내역을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 내역 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 결제 내역입니다.")
    })
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
    @Operation(summary = "본인 결제 내역 기간 페이징 조회", description = "프론트가 준 시작일과 종료일 범위 안에서 로그인 고객 본인의 영수증 리스트를 최신순 목록 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "본인 결제 내역 목록 조회 성공")
    })
    public ResponseEntity<com.example.delivery.global.common.response.ApiResponse<PageResponse<ResPaymentDto>>> getMyPayments(
            @Valid @ModelAttribute ReqPaymentSearchDto searchDto,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUserId();

        // 🌟 중복 정리 반영: 팩토리 객체에서 단 한 번만 안전하게 공통 페이징 세팅 수행!
        Pageable pageable = PageableFactory.of(page, size, sortBy, direction);

        Page<ResPaymentDto> pageData = paymentService.getMyPaymentsByPeriod(userId, searchDto, pageable);
        PageResponse<ResPaymentDto> pageResponse = PageResponse.from(pageData);
        return ResponseEntity.ok(com.example.delivery.global.common.response.ApiResponse.success("본인 결제 내역 목록 조회 성공", pageResponse));
    }

}
