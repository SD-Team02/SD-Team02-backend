package com.example.delivery.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 임시 SecurityFilterChain. 인증 담당자가 JWT 필터/역할별 인가 규칙을 붙이기 전까지
 * 개발/테스트가 막히지 않도록 전체 permitAll로 열어둔다.
 * TODO(인증 담당자): JWT 인증 필터 추가, 역할별 접근 제어(@PreAuthorize와 연계) 필요.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}