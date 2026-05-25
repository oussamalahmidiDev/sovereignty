package com.oussama.sovereignty.infrastructure.adapters.out.persistence;

import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import com.oussama.sovereignty.application.aop.TimedStep;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.PersistentSimpleVectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "sovereignty.vector-store.provider", havingValue = "internal", matchIfMissing = true)
public class SimpleVectorStoreAdapter implements VectorStorePort {

    private final VectorStore simpleVectorStore;

    @Override
    public void embed(UUID documentId, List<String> contents) {
        List<Document> documents = contents.stream()
                .map(content -> new Document(content, Map.of("documentId", documentId.toString())))
                .toList();
        simpleVectorStore.add(documents);
    }

    @Override
    public void clean(UUID documentId) {
        if (simpleVectorStore instanceof PersistentSimpleVectorStore persistentStore) {
            persistentStore.cleanByDocumentId(documentId);
        }
    }

    @Override
    @TimedStep("rag.topK.duration")
    public List<String> findTopSimilar(String question, int limit) {
        return simpleVectorStore.similaritySearch(
                        SearchRequest.builder().query(question).topK(limit).build()
                ).stream()
                .map(Document::getFormattedContent)
                .toList();
    }
}
