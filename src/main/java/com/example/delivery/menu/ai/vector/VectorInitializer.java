package com.example.delivery.menu.ai.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VectorInitializer implements ApplicationRunner {

    private final VectorStore vectorStore;

    @Override
    public void run(ApplicationArguments args) {

        load("korean.md", "korean");
        load("chinese.md", "chinese");
        load("japanese.md", "japanese");
        load("western.md", "western");
        load("chicken.md", "chicken");
        load("pizza.md", "pizza");
        load("snack.md", "snack");
        load("dessert.md", "dessert");
        load("cafe.md", "cafe");
//        load("beverage.md", "beverage");

        log.info("RAG 초기화 완료");
    }

    private void load(String fileName, String category){

        // 기존 파일 문서 삭제
        vectorStore.delete("file == '" + fileName + "'");

        // Markdown 문서 읽기
        MarkdownDocumentReader reader =
                new MarkdownDocumentReader(
                        "classpath:/rag/" + fileName
                );

        List<Document> docs = reader.get();

        // 검색용 메타데이터 추가
        docs.forEach(document -> {
            document.getMetadata().put("category", category);
            document.getMetadata().put("file", fileName);
        });
//        docs.forEach(document -> {
//
//            Map<String,Object> metadata = new HashMap<>();
//
//            metadata.put("category", category);
//            metadata.put("file", fileName);
//
//            document.getMetadata().putAll(metadata);
//
//        });

        vectorStore.add(docs);

        log.info("{} 저장 완료", fileName);

    }

}