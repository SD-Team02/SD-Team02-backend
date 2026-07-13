package com.example.delivery.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 승인 - 성공")
    void approvePayment_Success() {
        // given
        UUID orderId = UUID.randomUUID();
        Long userId = 1L;
        Integer amount = 25000;

        ReqApprovePaymentDto requestDto = createReqApproveDto(orderId, amount, "CARD", "신한카드");

        Order order = new Order(userId, UUID.randomUUID(), "서울 주소", "상세 주소", amount);
        ReflectionTestUtils.setField(order, "orderId", orderId);

        when(orderRepository.findByOrderIdAndDeletedAtIsNull(any(UUID.class))).thenReturn(Optional.of(order));

        // when
        ResApprovePaymentDto result = paymentService.approve(requestDto, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 승인 - 권한 없음 에러 발생")
    void approvePayment_AccessDenied_UserMismatch() {
        // given
        UUID orderId = UUID.randomUUID();
        Long loginUserId = 1L;
        Long orderOwnerId = 999L; // 로그인 고객과 주문 소유자 불일치

        ReqApprovePaymentDto requestDto = createReqApproveDto(orderId, 25000, "CARD", "신한카드");

        Order order = new Order(orderOwnerId, UUID.randomUUID(), "서울 주소", "상세 주소", 25000);
        ReflectionTestUtils.setField(order, "orderId", orderId);

        when(orderRepository.findByOrderIdAndDeletedAtIsNull(any(UUID.class))).thenReturn(Optional.of(order));

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.approve(requestDto, loginUserId);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("결제 내역 단건 조회 - 성공")
    void getPaymentById_Success() {
        // given
        UUID paymentId = UUID.randomUUID();
        Long userId = 1L;
        Role role = Role.CUSTOMER;

        Order order = new Order(userId, UUID.randomUUID(), "서울 주소", "상세 주소", 25000);
        ReflectionTestUtils.setField(order, "orderId", UUID.randomUUID());

        Payment payment = new Payment(order, "CARD", "국민카드", 25000);
        ReflectionTestUtils.setField(payment, "paymentId", paymentId);

        when(paymentRepository.findByPaymentIdAndDeletedAtIsNull(paymentId)).thenReturn(Optional.of(payment));

        // when
        ResPaymentDto result = paymentService.getPaymentById(paymentId, userId, role);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentId()).isEqualTo(paymentId);
        assertThat(result.getAmount()).isEqualTo(25000);
    }

    @Test
    @DisplayName("결제 취소(Soft Delete) - 성공")
    void cancelPayment_Success() {
        // given
        UUID paymentId = UUID.randomUUID();
        Long userId = 1L;
        Role role = Role.CUSTOMER;

        Order order = new Order(userId, UUID.randomUUID(), "서울 주소", "상세 주소", 25000);
        ReflectionTestUtils.setField(order, "orderId", UUID.randomUUID());
        ReflectionTestUtils.setField(order, "status", OrderStatus.CANCELED); // 주문 취소 완료 상태 선결

        Payment payment = new Payment(order, "CARD", "국민카드", 25000);
        payment.approve();
        ReflectionTestUtils.setField(payment, "paymentId", paymentId);

        when(paymentRepository.findByPaymentIdAndDeletedAtIsNull(paymentId)).thenReturn(Optional.of(payment));

        // when
        paymentService.cancel(paymentId, userId, role);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(payment.getDeletedAt()).isNotNull();
        assertThat(payment.getDeletedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("결제 취소(Soft Delete) - 주문이 취소 상태가 아닐 때 환불 거부 에러")
    void cancelPayment_ThrowException_OrderNotCanceled() {
        // given
        UUID paymentId = UUID.randomUUID();
        Long userId = 1L;
        Role role = Role.CUSTOMER;

        Order order = new Order(userId, UUID.randomUUID(), "서울 주소", "상세 주소", 25000);
        ReflectionTestUtils.setField(order, "orderId", UUID.randomUUID());
        ReflectionTestUtils.setField(order, "status", OrderStatus.ACCEPTED); // 주문 접수 상태 오류 연출

        Payment payment = new Payment(order, "CARD", "국민카드", 25000);
        ReflectionTestUtils.setField(payment, "paymentId", paymentId);

        when(paymentRepository.findByPaymentIdAndDeletedAtIsNull(paymentId)).thenReturn(Optional.of(payment));

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.cancel(paymentId, userId, role);
        });
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_STATUS_TRANSITION_INVALID);
    }

    @Test
    @DisplayName("본인 결제 내역 기간 조회 - 성공")
    void getMyPaymentsByPeriod_Success() {
        // given
        Long userId = 1L;
        ReqPaymentSearchDto searchDto = new ReqPaymentSearchDto();
        searchDto.setStartDate(LocalDate.of(2026, 7, 1));
        searchDto.setEndDate(LocalDate.of(2026, 7, 8));

        Pageable pageable = PageRequest.of(0, 10);

        Order order = new Order(userId, UUID.randomUUID(), "서울 주소", "상세 주소", 25000);
        ReflectionTestUtils.setField(order, "orderId", UUID.randomUUID());

        Payment payment = new Payment(order, "CARD", "신한카드", 25000);
        Page<Payment> mockPage = new PageImpl<>(List.of(payment), pageable, 1);

        when(paymentRepository.findMyPaymentsByPeriod(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(mockPage);

        // when
        Page<ResPaymentDto> result = paymentService.getMyPaymentsByPeriod(userId, searchDto, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAmount()).isEqualTo(25000);
    }

    @SneakyThrows
    private ReqApprovePaymentDto createReqApproveDto(UUID orderId, Integer amount, String method, String company) {
        Constructor<ReqApprovePaymentDto> constructor = ReqApprovePaymentDto.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ReqApprovePaymentDto dto = constructor.newInstance();
        ReflectionTestUtils.setField(dto, "orderId", orderId);
        ReflectionTestUtils.setField(dto, "amount", amount);
        ReflectionTestUtils.setField(dto, "paymentMethod", method);
        ReflectionTestUtils.setField(dto, "cardCompany", company);
        return dto;
    }
}
