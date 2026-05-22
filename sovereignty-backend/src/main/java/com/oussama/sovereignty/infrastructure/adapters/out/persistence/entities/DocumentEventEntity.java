package com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentEventEntity {

    @Id
    private UUID id;

    private UUID documentId;
    private String eventType;
    private String traceId;
    private String failureReason;
    private LocalDateTime createdAt;
}
