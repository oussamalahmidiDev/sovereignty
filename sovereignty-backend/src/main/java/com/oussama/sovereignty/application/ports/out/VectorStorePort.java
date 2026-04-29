package com.oussama.sovereignty.application.ports.out;

import java.util.List;
import java.util.UUID;

public interface VectorStorePort {
    void save(UUID documentId, String content);

    List<String> findTopSimilar(String question, int limit);
}
