package com.oussama.sovereignty.infrastructure.adapters.out.persistence;

import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PgVectorAdapter implements VectorStorePort {

    private final VectorStore pgVectorStore;

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public void save(UUID documentId, String content) {

        Document document = new Document(content, Map.of("documentId", documentId.toString()));

        pgVectorStore.add(List.of(document));
    }

    @Override
    @Transactional
    public void clean(UUID documentId) {
        entityManager.createNativeQuery(
                "DELETE FROM vector_store WHERE metadata->>'documentId' = :documentId"
        )
                .setParameter("documentId", documentId.toString())
                .executeUpdate();
    }

    @Override
    public List<String> findTopSimilar(String question, int limit) {
        return pgVectorStore.similaritySearch(
                        SearchRequest.builder().query(question).topK(limit).build()
                ).stream()
                .map(Document::getFormattedContent)
                .toList();
    }
}
