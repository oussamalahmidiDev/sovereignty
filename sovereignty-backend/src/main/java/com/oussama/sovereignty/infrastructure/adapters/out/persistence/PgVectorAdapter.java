package com.oussama.sovereignty.infrastructure.adapters.out.persistence;

import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import com.oussama.sovereignty.infrastructure.aop.TimedStep;
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
    public void embed(UUID documentId, String content) {

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

    /**
     * Performs a semantic similarity search against the vector store
     * in order to retrieve the most relevant document chunks for a user question.
     *
     * <p>The question is automatically transformed into an embedding vector
     * using the configured embedding model. The vector store then performs
     * a nearest-neighbor similarity search against stored document embeddings
     * and returns the Top-K most semantically relevant chunks.</p>
     *
     * @param question the user question
     * @param limit the maximum number of similar chunks to retrieve (Top-K)
     * @return a list containing the textual content of the most relevant chunks
     */
    @Override
    @TimedStep("rag.topK.duration")
    public List<String> findTopSimilar(String question, int limit) {
        return pgVectorStore.similaritySearch(
                        SearchRequest.builder().query(question).topK(limit).build()
                ).stream()
                .map(Document::getFormattedContent)
                .toList();
    }
}
