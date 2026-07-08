package com.example.delivery.payment.controller;

import com.example.delivery.global.common.response.ApiResponse;
import com.example.delivery.payment.dto.request.ReqApprovePaymentDto;
import com.example.delivery.payment.dto.response.ResApprovePaymentDto;
import com.example.delivery.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/test")
    public ResponseEntity<String> testMapping() {
        // 1. 서버 콘솔창에 로그가 찍히는지 확인
        System.out.println("=========================================");
        System.out.println("★ 대박! 결제 컨트롤러 URL 매핑 호출 성공! ★");
        System.out.println("=========================================");

        // 2. 브라우저 화면에 보일 글자 리턴
        return ResponseEntity.ok("Payment Controller Connection OK!");
    }

    /**
     * 1. 결제 승인 API
     * 현재 로그인 기능이 없어 Authentication이 null로 들어와서 처리
     * JWT인증 생기면 수정해야함
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ResApprovePaymentDto>> approvePayment(
            @Valid @RequestBody ReqApprovePaymentDto requestDto,
            Authentication authentication // 시큐리티 인증 객체 주입(일단 해놓음)
    ) {
        // 임시 단계: 로그인이 안 되어 있으면 가상으로 유저 고유 ID를 0L로 설정합니다.
        Long userId = 0L;

        // 나중에 JWT 로그인이 완성되면 아래 주석만 해제
        /*
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            userId = userDetails.getUserId(); //  팀의 CustomUserDetails 규격에서 Long 유저 ID 추출
        }
        */

        // 서비스단으로 주문 DTO와 추출한 유저 ID를 함께 전달하여 비즈니스 수행
        ResApprovePaymentDto responseData = paymentService.approve(requestDto, userId);

        return ResponseEntity.ok(ApiResponse.success("결제 승인 성공", responseData));
    }

}
