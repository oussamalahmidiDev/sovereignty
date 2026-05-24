package com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities;

import com.oussama.sovereignty.domain.model.OutboxEvent.OutboxStatus;
import com.oussama.sovereignty.domain.model.OutboxEvent.AggregateType;
import com.oussama.sovereignty.domain.model.OutboxEvent.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboxEventEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private AggregateType aggregateType;

    private String aggregateId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String traceId;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
