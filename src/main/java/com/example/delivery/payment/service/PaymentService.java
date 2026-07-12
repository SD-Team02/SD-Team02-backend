package com.example.delivery.payment.service;

import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderStatus;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.payment.dto.request.ReqApprovePaymentDto;
import com.example.delivery.payment.dto.request.ReqPaymentSearchDto;
import com.example.delivery.payment.dto.response.ResApprovePaymentDto;
import com.example.delivery.payment.dto.response.ResPaymentDto;
import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentStatus;
import com.example.delivery.payment.repository.PaymentRepository;
import com.example.delivery.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /** 1. 결제 승인 비즈니스 로직  */
    @Transactional
    public ResApprovePaymentDto approve(ReqApprovePaymentDto requestDto, Long userId) {
        Order order = orderRepository.findByOrderIdAndDeletedAtIsNull(requestDto.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 로그인한 유저 ID와 주문 생성자 ID를 검증
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED); // 권한 없는 결제 시도 차단
        }

        // 주문 가격과 실제 결제 가격 비교
        if (!order.getTotalPrice().equals(requestDto.getAmount())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Payment payment = new Payment(order, requestDto.getPaymentMethod(), requestDto.getCardCompany(), requestDto.getAmount());
        payment.approve();
        paymentRepository.save(payment);
        order.changeStatus(OrderStatus.ACCEPTED); // 주문 상태 ACCEPTED 처리

        return new ResApprovePaymentDto(payment);
    }

    /** 2. 결제 내역 단건 조회 */
    public ResPaymentDto getPaymentById(UUID paymentId, Long userId, Role role) {
        Payment payment = paymentRepository.findByPaymentIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        validatePaymentAccess(payment, userId, role);

        return new ResPaymentDto(payment);
    }

    /** 3. 고객 본인용: 기간 범위별 목록 조회  */
    public Page<ResPaymentDto> getMyPaymentsByPeriod(Long userId, ReqPaymentSearchDto searchDto, Pageable pageable) {
        LocalDateTime startDateTime = searchDto.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = searchDto.getEndDate().atTime(LocalTime.MAX);

        Page<Payment> paymentPage = paymentRepository.findMyPaymentsByPeriod(userId, startDateTime, endDateTime, pageable);
        return paymentPage.map(ResPaymentDto::new);
    }

    /** 4. 관리자용 조건 검색 */
    public Page<ResPaymentDto> getAdminPaymentsByFilters(ReqPaymentSearchDto searchDto, Pageable pageable) {
        LocalDateTime startDateTime = searchDto.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = searchDto.getEndDate().atTime(LocalTime.MAX);

        Page<Payment> paymentPage = paymentRepository.findAdminPaymentsByFilters(
                startDateTime, endDateTime, searchDto.getStatus(), pageable
        );
        return paymentPage.map(ResPaymentDto::new);
    }

    /** 5. 결제 취소  */
    @Transactional
    public void cancel(UUID paymentId, Long userId, Role role) {
        Payment payment = paymentRepository.findByPaymentIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.CANCELED) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND); // #TODO : 에러코드 추가 요망 ALREADY_CANCELED_PAYMENT
        }

        validatePaymentAccess(payment, userId, role);

        Order order = payment.getOrder();
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.CANCELED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_TRANSITION_INVALID); // #TODO : 에러코드 추가 요망 ORDER_NOT_CANCELED_YET
        }

        payment.softDelete(userId);
        payment.cancel();
    }


    private void validatePaymentAccess(Payment payment, Long userId, Role role) {
        switch (role) {
            case CUSTOMER -> {
                if (!payment.getOrder().getUserId().equals(userId)) {
                    throw new BusinessException(ErrorCode.ACCESS_DENIED);
                }
            }
            case OWNER -> {
                // 임시로 사장님 검증 영역을 열어둠
            }
            case MANAGER, MASTER -> {
                // 전체 접근 허용
            }
        }
    }

}
