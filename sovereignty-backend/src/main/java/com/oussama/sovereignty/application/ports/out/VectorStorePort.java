package com.oussama.sovereignty.application.ports.out;

import java.util.List;
import java.util.UUID;

public interface VectorStorePort {
    void embed(UUID documentId, String content);

    void clean(UUID documentId);

    List<String> findTopSimilar(String question, int limit);
}
