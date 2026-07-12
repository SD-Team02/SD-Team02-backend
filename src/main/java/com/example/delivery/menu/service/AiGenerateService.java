package com.example.delivery.menu.service;

import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.global.exception.BusinessException;
import com.example.delivery.global.exception.ErrorCode;
import com.example.delivery.image.service.ImageService;
import com.example.delivery.menu.ai.prompt.MenuPromptBuilder;
import com.example.delivery.menu.ai.rag.RagService;
import com.example.delivery.menu.dto.request.AiGenerateRequestDto;
import com.example.delivery.menu.entity.AiHistory;
import com.example.delivery.menu.repository.AiHistoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerateService {
    //RAG Service 호출
    private final RagService ragService;
    //Prompt Builder 호출
    private final MenuPromptBuilder promptBuilder;
    //LLM 호출
    private final ChatClient chatClient;
    //이미지 가져오기
    private final ImageService imageService;
    //ai history repository 가져오기
    private final AiHistoryRepository aiHistoryRepository;

    public String generateDescription(@Valid AiGenerateRequestDto aiGenerateRequestDto, JpaAuditingConfig.CustomUserDetails userDetails) {

        Resource image = imageService.getImageResource(aiGenerateRequestDto.getImageId());

        // 1. RAG 검색
        String context = ragService.retrieveContext(aiGenerateRequestDto);
        // 2. Prompt 생성
        String prompt = promptBuilder.build(aiGenerateRequestDto, context);

        String answer = "";

        log.info("RAG context : " + context);
        log.info("Prompt 생성 prompt : " + prompt);
        // 이미지 가져오는지 확인
        log.info("Image Resource = {}", image.getDescription());
        log.info("Image Exists = {}", image.exists());

        // 3. Gemini 호출
        try {
            answer = chatClient.prompt()
                .user(u -> u
                        .text(prompt)
                        .media(MimeTypeUtils.IMAGE_JPEG, image)
                )
                .call()
                .content();

            // 4. History 저장
            saveHistory(aiGenerateRequestDto, answer);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
        }

        return answer;
    }

    private void saveHistory(
            AiGenerateRequestDto request,
            String answer
    ) {

        AiHistory aiHistory = new AiHistory(
                request.getPrompt(),
                answer
            );
        log.info("aiHistory 저장 prompt : " + request.getPrompt() + "\nanswer : " + answer);

        aiHistoryRepository.save(aiHistory);
    }
}
