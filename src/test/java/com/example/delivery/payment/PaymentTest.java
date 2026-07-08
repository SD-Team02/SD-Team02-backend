package com.example.delivery.payment;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.delivery.payment.dto.request.ReqApprovePaymentDto;
import com.example.delivery.payment.dto.response.ResApprovePaymentDto;
import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentStatus;
import com.example.delivery.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
@SpringBootTest
@AutoConfigureMockMvc // 컨트롤러에 HTTP 요청을 가상으로 보낼 수 있는 툴 주입
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // 자바 객체를 JSON 글자로 변환해주는 도구

    @MockitoBean // 🌟 중요: 복잡한 DB 연동 및 비즈니스 레이어를 가짜(Mock) 객체로 대체
    private PaymentService paymentService;

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER") // 🛡️ 팀 시큐리티 권한 장벽 완벽 통과 우회
    @DisplayName("결제 승인 API가 팀 컨벤션 및 ApiResponse 스펙에 맞게 정상 응답한다")
    void approvePayment_Success() throws Exception {
        // given (가짜 데이터 준비 단계)
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        // 1. 포스트맨으로 보낼 가짜 JSON 요청 객체 생성 (25000원 결제)
        ReqApprovePaymentDto requestDto = createReqDto(orderId, 25000, "CARD", "신한카드");

        // 2. 리플렉션을 이용해 ResApprovePaymentDto 가짜 응답 객체 조립 (from 패턴 스펙 매싱)
        ResApprovePaymentDto mockResponse = createResDto(paymentId, orderId, PaymentStatus.SUCCESS, LocalDateTime.now());

        // 3. "서비스단아, 누가 결제 요청하면 실제 검증 거치지 말고 무조건 이 가짜 응답 리턴해!" 라고 약속 지정
        given(paymentService.approve(any(ReqApprovePaymentDto.class), eq(0L)))
                .willReturn(mockResponse);

        // when & then (실제 가상 발사 및 검증 단계)
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))) // 가상으로 JSON 본문 투척
                .andExpect(status().isOk()) // 1. HTTP 200 OK 상태 코드 검증

                // 2. 올려주신 사진의 ApiResponse 공통 포맷 속성 검증
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("결제 등록 성공")) // 명세서 하드코딩 메시지 일치 확인

                // 3. 팀 컨벤션 규칙 가이드인 camelCase JSON 필드명 일치 여부 정밀 검증
                .andExpect(jsonPath("$.data.paymentId").value(paymentId.toString())) // payment_id 오타 완벽 제거 검증
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andDo(print()); // 콘솔창에 변환된 JSON 최종 모양을 예쁘게 출력
    }

    // 리플렉션을 활용해 빈 생성자가 막힌 DTO 객체들을 안전하게 생성하는 테스트 유틸리티 메서드
    @SneakyThrows
    private ReqApprovePaymentDto createReqDto(UUID orderId, Integer amount, String method, String company) {
        Constructor<ReqApprovePaymentDto> constructor = ReqApprovePaymentDto.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ReqApprovePaymentDto dto = constructor.newInstance();

        setField(dto, "orderId", orderId);
        setField(dto, "amount", amount);
        setField(dto, "paymentMethod", method);
        setField(dto, "cardCompany", company);
        return dto;
    }

    @SneakyThrows
    private ResApprovePaymentDto createResDto(UUID paymentId, UUID orderId, PaymentStatus status, LocalDateTime approvedAt) {
        Constructor<ResApprovePaymentDto> constructor = ResApprovePaymentDto.class.getDeclaredConstructor(UUID.class, UUID.class, PaymentStatus.class, LocalDateTime.class);
        constructor.setAccessible(true);
        return constructor.newInstance(paymentId, orderId, status, approvedAt);
    }

    @SneakyThrows
    private void setField(Object obj, String fieldName, Object value) {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}