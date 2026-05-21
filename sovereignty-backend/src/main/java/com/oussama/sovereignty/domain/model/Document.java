package com.oussama.sovereignty.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Document(
        UUID id,
        String fileName,
        String contentType,
        DocumentStatus status,
        LocalDateTime createdAt
) {

    public Document copyAndChangeStatus(DocumentStatus newStatus) {
        return new Document(
                id(),
                fileName(),
                contentType(),
                newStatus,
                createdAt()
        );
    }

    public enum DocumentStatus {
        UPLOADED,
        PROCESSING,
        READY,
        FAILED;
    }

}
