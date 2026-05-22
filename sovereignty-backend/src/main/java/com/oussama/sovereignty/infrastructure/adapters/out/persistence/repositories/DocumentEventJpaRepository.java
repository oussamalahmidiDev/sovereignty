package com.oussama.sovereignty.infrastructure.adapters.out.persistence.repositories;

import com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities.DocumentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentEventJpaRepository extends JpaRepository<DocumentEventEntity, UUID> {
    List<DocumentEventEntity> findByDocumentIdInAndEventType(List<UUID> documentIds, String eventType);
    List<DocumentEventEntity> findByDocumentIdAndEventType(UUID documentId, String eventType);
}
