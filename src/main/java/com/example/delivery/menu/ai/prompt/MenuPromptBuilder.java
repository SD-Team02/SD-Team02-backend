package com.example.delivery.menu.ai.prompt;

import com.example.delivery.menu.dto.request.AiGenerateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class MenuPromptBuilder {

    public String build(AiGenerateRequestDto request,
                        String context){

        return """
                당신은 배달 플랫폼의 메뉴 소개 전문가입니다.
                
                첨부된 음식 이미지를 먼저 분석하세요.
                이미지에서 보이는 음식의 특징을 바탕으로 메뉴 설명을 작성하세요.
                참고 문서는 표현 스타일과 카테고리 톤을 맞추는 용도로만 사용하세요.
                ====================
                참고 문서
                %s
                ====================
                카테고리
                %s
                
                메뉴명
                %s
                
                사용자 요청
                %s
                ====================                
                규칙
                - 이미지에서 확인되지 않는 재료는 단정하지 마세요.
                - 200자 이내
                - 고객이 주문하고 싶도록 작성
                - 과장 금지
                - 자연스럽게 작성
               """
        .formatted(
            context,
            request.getCategory(),
            request.getMenuName(),
            request.getPrompt()
        );

    }

}