package com.example.delivery.global.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @CreatedBy / @LastModifiedBy 가 사용할 "현재 로그인 유저의 user_id(Long)"를 제공한다.
 * p_user의 PK가 user_id(LONG, auto-increment)임에 따라
 * created_by / updated_by / deleted_by도 전부 Long(user_id)을 저장하도록 통일한다.
 *
 * 인증 담당자와 논의:
 * - 로그인 성공 시 SecurityContext에 채워지는 Authentication의 principal은
 *   user_id(Long)를 꺼낼 수 있는 커스텀 UserDetails 구현체(예: CustomUserDetails)여야 한다.
 * - 예) authentication.getPrincipal() instanceof CustomUserDetails 이고,
 *   ((CustomUserDetails) principal).getUserId()가 Long 값을 반환해야 한다.
 * - 그 전까지는 SYSTEM_USER_ID(0L)로 대체한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    // 배치/스케줄러 등 인증 컨텍스트가 없는 경우, 혹은 인증 principal 타입이 아직 미정 상태일 때 사용하는 고정 값
    private static final Long SYSTEM_USER_ID = 0L;

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of(SYSTEM_USER_ID);
            }

            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails customUserDetails) {
                return Optional.of(customUserDetails.getUserId());
            }

            // 인증 담당자가 CustomUserDetails를 아직 안 만들었을 경우를 대비한 안전장치
            return Optional.of(SYSTEM_USER_ID);
        };
    }

    /**
     * 인증 담당자가 실제로 구현할 인터페이스 계약(placeholder).
     * 실제 구현체는 인증 담당자의 패키지(예: auth/security/CustomUserDetails)에 위치시키고,
     * 이 인터페이스를 implements 하도록 안내한다.
     */
    public interface CustomUserDetails {
        Long getUserId();
    }
}