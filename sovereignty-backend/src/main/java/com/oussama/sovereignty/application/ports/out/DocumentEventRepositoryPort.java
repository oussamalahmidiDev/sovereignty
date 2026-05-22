package com.oussama.sovereignty.application.ports.out;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DocumentEventRepositoryPort {
    void saveEvent(UUID documentId, String eventType, String traceId, String failureReason);

    Map<UUID, EventDetails> findFailureDetailsByDocumentIds(List<UUID> documentIds);

    String findTraceIdByDocumentId(UUID documentId);

    record EventDetails(String traceId, String failureReason) {}
}
