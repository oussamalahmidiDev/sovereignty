package com.oussama.sovereignty.infrastructure.adapters.out.persistence.mappers;

import com.oussama.sovereignty.domain.model.OutboxEvent;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities.OutboxEventEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OutboxEventEntityMapper {

    public static OutboxEventEntity toEntity(OutboxEvent event) {
        return OutboxEventEntity.builder()
                .id(event.id())
                .aggregateType(event.aggregateType())
                .aggregateId(event.aggregateId())
                .eventType(event.eventType())
                .payload(event.payload())
                .traceId(event.traceId())
                .status(event.status())
                .createdAt(event.createdAt())
                .processedAt(event.processedAt())
                .build();
    }

    public static OutboxEvent toDomain(OutboxEventEntity entity) {
        return new OutboxEvent(
                entity.getId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getEventType(),
                entity.getPayload(),
                entity.getTraceId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getProcessedAt()
        );
    }
}
