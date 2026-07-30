package com.example.delivery.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class AiGenerateRequestDto {

    // AI 답변생성
    @NotNull(message = "메뉴 이미지 ID는 필수입니다.")
    private UUID imageId;               // 이미지 전송
    @NotBlank (message = "메뉴 이름은 필수입니다.")
    private String menuName;            // 메뉴 이름
    @NotBlank(message = "프롬프트는 필수입니다.")
    @Size(max = 300, message = "요청 텍스트는 300자 이하로 입력해주세요.")
    private String prompt;              // AI에게 명령한 프롬프트
    private String category;            // (한식, 중식, 일식....)

}
