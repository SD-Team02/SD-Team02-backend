package com.example.delivery.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * springdoc-openapi 설정.
 * 로컬 실행 후 http://localhost:8080/swagger-ui.html 에서 확인.
 *
 * 각 도메인 담당자는 컨트롤러에 @Tag, @Operation, @Parameter, @ApiResponse(io.swagger 쪽) 등을
 * 붙여서 문서를 채워나간다. 최소한 아래는 꼭 넣어주세요:
 *  - @Tag(name = "가게") 처럼 도메인별 태그
 *  - @Operation(summary = "...") 로 각 엔드포인트 설명
 *  - Request/Response DTO에 @Schema(description = "...") 로 필드 설명
 */
@Configuration
public class SwaggerConfig {

    private static final String JWT_SCHEME_NAME = "JWT";

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme jwtScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(JWT_SCHEME_NAME);

        return new OpenAPI()
                .servers(List.of(
                    new Server().url("https://meogjago.shop").description("실제 서버"),
                    new Server().url("http://localhost:8080").description("로컬 개발")
                ))
                .info(apiInfo())
                .components(new Components().addSecuritySchemes(JWT_SCHEME_NAME, jwtScheme))
                .addSecurityItem(securityRequirement);
    }

    private Info apiInfo() {
        return new Info()
                .title("배달 주문 관리 플랫폼 API")
                .description("광화문 지역 배달 주문 관리 플랫폼 백엔드 API 명세서. "
                        + "요청 헤더 Authorization: Bearer {JWT} 형식으로 인증합니다.")
                .version("v1.0.0")
                .contact(new Contact().name("공통/인프라 담당").email("qkrtnqls5676@gmail.com"));
    }
}