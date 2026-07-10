package com.example.delivery.payment.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.global.common.response.PageResponse;
import com.example.delivery.global.common.util.PageableFactory;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.payment.dto.request.ReqApprovePaymentDto;
import com.example.delivery.payment.dto.request.ReqPaymentSearchDto;
import com.example.delivery.payment.dto.response.ResApprovePaymentDto;
import com.example.delivery.payment.dto.response.ResPaymentDto;
import com.example.delivery.payment.service.PaymentService;
import com.example.delivery.user.security.UserDetailsImpl;
import com.example.delivery.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "결제", description = "결제 API")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

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
        Long userId = userService.getCurrentUserId(userDetails);

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
            @PathVariable("paymentId") UUID paymentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.getCurrentUserId(userDetails);

        ResPaymentDto response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponse.success("결제 내역 조회 성공", response));
    }

    /** 3. 고객 본인용: 기간 범위별 목록 조회
     * JWT인증 생기면 확인해야함
     * */
    @GetMapping("/customer")
    @Operation(summary = "본인 결제 내역 기간 페이징 조회", description = "프론트가 준 시작일과 종료일 범위 안에서 로그인 고객 본인의 영수증 리스트를 최신순 목록 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "본인 결제 내역 목록 조회 성공")
    })
    public ResponseEntity<ApiResponse<PageResponse<ResPaymentDto>>> getMyPayments(
            @Valid @ModelAttribute ReqPaymentSearchDto searchDto,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userService.getCurrentUserId(userDetails);

        Pageable pageable = PageableFactory.of(page, size, sortBy, direction);

        Page<ResPaymentDto> pageData = paymentService.getMyPaymentsByPeriod(userId, searchDto, pageable);
        PageResponse<ResPaymentDto> pageResponse = PageResponse.from(pageData);
        return ResponseEntity.ok(com.example.delivery.global.common.response.ApiResponse.success("본인 결제 내역 목록 조회 성공", pageResponse));
    }

    /** 3. 관리자용: 기간 범위별 목록 조회  */
    @GetMapping
    @Operation(summary = "관리자용 전체 결제 내역 검색 목록 조회", description = "결제를 기간 및 결제 상태(ENUM) 페이징 검색합니다. (MANAGER/MASTER 권한 전용)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "관리자용 결제 내역 검색 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 전용 접근 거부 에러")
    })
    public ResponseEntity<ApiResponse<PageResponse<ResPaymentDto>>> getAdminPayments(
            @Valid @ModelAttribute ReqPaymentSearchDto searchDto,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {

        try {
            userService.validateManager(userDetails);   // 매니저 권한 체크
        } catch (BusinessException e) {
            userService.validateMaster(userDetails);    // 마스터 권한 체크
        }

        Pageable pageable = PageableFactory.of(page, size, sortBy, direction);

        Page<ResPaymentDto> pageData = paymentService.getAdminPaymentsByFilters(searchDto, pageable);
        PageResponse<ResPaymentDto> pageResponse = PageResponse.from(pageData);
        return ResponseEntity.ok(com.example.delivery.global.common.response.ApiResponse.success("관리자용 결제 내역 검색 성공", pageResponse));
    }

    @PostMapping("/{paymentId}/cancel")
    @Operation(summary = "결제 취소 처리 (환불)", description = "결제 건의 상태를 CANCELED로 바꾸고 Soft Delete 처리")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 환불 완료되었거나 5분이 경과한 주문건입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "결제 취소 권한 거부")
    })
    public ResponseEntity<ApiResponse<Void>> cancelPayment(
            @PathVariable("paymentId") UUID paymentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userService.getCurrentUserId(userDetails);
        paymentService.cancel(paymentId, userId);
        return ResponseEntity.ok(com.example.delivery.global.common.response.ApiResponse.success("결제 취소 성공", null));
    }

}
