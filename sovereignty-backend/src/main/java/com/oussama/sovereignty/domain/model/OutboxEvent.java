package com.oussama.sovereignty.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record OutboxEvent(
    UUID id,
    AggregateType aggregateType,
    String aggregateId,
    EventType eventType,
    String payload,
    String traceId,
    OutboxStatus status,
    LocalDateTime createdAt,
    LocalDateTime processedAt
) {
    public enum OutboxStatus {
        PENDING,
        PROCESSED,
        FAILED
    }

    public enum AggregateType {
        DOCUMENT
    }

    public enum EventType {
        DOCUMENT_UPLOADED
    }
}
