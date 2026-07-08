package com.example.delivery.payment.service;

import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.order.entity.OrderStatus;
import com.example.delivery.payment.dto.request.ReqApprovePaymentDto;
import com.example.delivery.payment.dto.response.ResApprovePaymentDto;
import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository; // 도메인 직접 참조 지양 규칙에 따른 주입

    /**
     * 1. 결제 승인 비즈니스 로직 (유저 ID 검증 포함)
     */
    @Transactional
    public ResApprovePaymentDto approve(ReqApprovePaymentDto requestDto, Long userId) {

        // 1. 주문 파트 담당자가 생성해둔 주문 정보 조회 -> order 쪽 보고 수정해야함
//        Order order = orderRepository.findById(requestDto.getOrderId())
//                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 2. 유저 보안 검증: 주문을 생성한 사람과 현재 결제를 요청한 사람이 일치하는지 체크
        // 현재는 임시 개발 단계(userId = 0L)이므로 시스템 계정(0L)일 때는 검증을 유연하게 통과시킵니다.
        if (!userId.equals(0L) && !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_PAYMENT_REQUEST); // 권한 없는 결제 시도 에러
        }

        // 3. 금액 위변조 검증: 주문 테이블의 실제 총 가격과 사용자가 요청한 금액이 일치하는지 대조
        if (!order.getTotalPrice().equals(requestDto.getAmount())) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        // 4. 결제 엔티티 객체 생성 (기본 READY 상태)
        Payment payment = new Payment(
                order,
                requestDto.getPaymentMethod(),
                requestDto.getCardCompany(),
                requestDto.getAmount()
        );

        // [5] 가상 카드사 승인 프로세스 진행 (SUCCESS 상태 전환 및 승인시각 주입)
        payment.approve();
        paymentRepository.save(payment);

        // [6] 주문 파트 도메인 상태 동기화 (주문요청 -> 주문접수/ACCEPTED 상태로 변경)
        //order.changeStatus(OrderStatus.ACCEPTED);

        return ResApprovePaymentDto.from(payment);
    }
}
