package com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities;

import com.oussama.sovereignty.domain.model.Document.DocumentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentEntity {

    @Id
    private UUID id;

    private String fileName;
    private String contentType;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    private LocalDateTime createdAt;
}
