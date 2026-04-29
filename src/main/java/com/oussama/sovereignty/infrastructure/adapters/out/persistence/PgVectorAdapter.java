package com.oussama.sovereignty.infrastructure.adapters.out.persistence;

import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PgVectorAdapter implements VectorStorePort {

    private final VectorStore pgVectorStore;

    @Override
    public void save(UUID documentId, String content, float[] vector) {

        Document document = new Document(content, Map.of("documentId", documentId.toString()));

        pgVectorStore.add(List.of(document));
    }

    @Override
    public List<String> findTopSimilar(float[] queryVector, int limit) {
        return pgVectorStore.similaritySearch(
                        SearchRequest.builder().query("").topK(limit).build()
                ).stream()
                .map(Document::getFormattedContent)
                .toList();
    }
}
