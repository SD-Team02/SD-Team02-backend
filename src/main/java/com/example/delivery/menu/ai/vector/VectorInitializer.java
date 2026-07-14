package com.example.delivery.menu.ai.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class VectorInitializer implements ApplicationRunner {

    private final VectorStore vectorStore;

    @Override
    public void run(ApplicationArguments args) {

        load("korean.md", "한식");
        load("chinese.md", "중식");
        load("japanese.md", "일식");
        load("western.md", "양식");
        load("chicken.md", "치킨");
        load("pizza.md", "피자");
        load("cafe_dessert.md", "카페_디저트");
        load("donkatsu_sashimi.md", "돈까스_회");
        load("fast_food.md", "패스트푸드");
        load("jjim_tang.md", "찜_탕");
        load("jokbal_bossam.md", "족발_보쌈");
        load("bunsik.md", "분식");
        load("meat.md", "고기");
        load("asian.md", "아시안");
        load("late_night.md", "야식");
        load("dosirak.md", "도시락");

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

        vectorStore.add(docs);

        log.info("{} 저장 완료", fileName);

    }

}
