package com.oussama.sovereignty.application.ports.out;

import java.util.List;
import java.util.UUID;

public interface VectorStorePort {
    default void embed(UUID documentId, String content) {
        embed(documentId, List.of(content));
    }

    void embed(UUID documentId, List<String> contents);

    void clean(UUID documentId);

    List<String> findTopSimilar(String question, int limit);
}
