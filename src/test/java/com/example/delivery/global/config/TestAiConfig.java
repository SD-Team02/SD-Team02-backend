package com.example.delivery.global.config;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * test 프로파일 전용 AI 빈 구성 (src/test 전용, 운영 코드에는 포함되지 않음).
 *
 * 운영/로컬은 pgvector(RDS/docker-compose) + Transformers ONNX 임베딩 모델을 실제로 쓰지만,
 * 테스트는 H2를 써서 pgvector 자체가 불가능하고, CI 러너에서 ONNX 모델 로딩이
 * (네트워크/환경 문제로) ORT_INVALID_PROTOBUF 에러로 실패하는 경우가 있다.
 * RagServiceImpl 등이 VectorStore를 생성자 주입받기 때문에, 테스트에서는
 * 진짜 pgvector/ONNX 대신 이 가짜(인메모리, 고정 벡터) 빈으로 대체해서
 * 컨텍스트 로딩만 통과시킨다. 실제 검색 품질을 테스트하려는 목적이 아니라
 * "컨텍스트가 뜨는지"를 위한 최소 구성이다.
 */
@Configuration
@Profile("test")
public class TestAiConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new EmbeddingModel() {
            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                List<String> texts = request.getInstructions();
                List<Embedding> embeddings = new ArrayList<>();
                for (int i = 0; i < texts.size(); i++) {
                    embeddings.add(new Embedding(new float[]{0f}, i));
                }
                return new EmbeddingResponse(embeddings);
            }

            @Override
            public float[] embed(Document document) {
                return new float[]{0f};
            }
        };
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    // AiConfig.chatClient(ChatModel)가 빈을 요구하는데, 테스트엔 실제 AI_API_KEY/GCP project-id가
    // 없어서 GoogleGenAiChatAutoConfiguration이 실패한다(application-test.yml에서 제외 처리함).
    // 대신 이 가짜 ChatModel로 컨텍스트 로딩만 통과시킨다 (실제 생성 품질 테스트 목적 아님).
    @Bean
    public ChatModel chatModel() {
        return prompt -> new ChatResponse(
                List.of(new Generation(new AssistantMessage("test response")))
        );
    }
}
