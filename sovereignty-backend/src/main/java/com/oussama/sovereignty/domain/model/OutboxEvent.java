package com.oussama.sovereignty.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record OutboxEvent(
    UUID id,
    String aggregateType,
    String aggregateId,
    String eventType,
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
}
