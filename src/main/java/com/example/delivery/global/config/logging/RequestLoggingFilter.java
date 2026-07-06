package com.example.delivery.global.config.logging;

import java.io.IOException;
import java.util.UUID;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * 모든 요청/응답에 대해 method, URI, status, 처리시간(ms)을 로그로 남긴다 (도전 기능: 로깅).
 * MDC에 requestId를 심어서 하나의 요청에 대한 로그를 traceId로 묶어 조회할 수 있게 한다.
 */
@Slf4j
@Order(1)
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(REQUEST_ID_KEY, requestId);
        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[{}] {} {} -> {} ({}ms)",
                    requestId, request.getMethod(), request.getRequestURI(), response.getStatus(), elapsed);
            MDC.remove(REQUEST_ID_KEY);
        }
    }
}