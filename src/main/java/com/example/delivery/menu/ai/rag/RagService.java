package com.example.delivery.menu.ai.rag;

import com.example.delivery.menu.dto.request.AiGenerateRequestDto;
import jakarta.validation.Valid;

public interface RagService {

    String retrieveContext(@Valid AiGenerateRequestDto aiGenerateRequestDto);
}