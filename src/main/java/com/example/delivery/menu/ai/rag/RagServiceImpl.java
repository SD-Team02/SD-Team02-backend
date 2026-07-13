package com.example.delivery.menu.ai.rag;

import com.example.delivery.menu.dto.request.AiGenerateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final VectorStore vectorStore;

    @Override
    public String retrieveContext(AiGenerateRequestDto request) {

        SearchRequest searchRequest = SearchRequest.builder()
                .query(request.getPrompt())
                .topK(3)
                .filterExpression("category == '" + request.getCategory() + "'")
                .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        if (documents == null || documents.isEmpty()) {
            log.warn("검색된 RAG 문서가 없습니다. category={}", request.getCategory());
            return "";
        }

        documents.forEach(doc ->
                log.info("RAG 검색 결과 metadata={}", doc.getMetadata())
        );

        return documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
    }
}