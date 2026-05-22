package com.oussama.sovereignty.infrastructure.adapters.out.persistence;

import com.oussama.sovereignty.application.ports.out.DocumentEventRepositoryPort;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities.DocumentEventEntity;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.repositories.DocumentEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DocumentEventPersistenceAdapter implements DocumentEventRepositoryPort {

    private final DocumentEventJpaRepository repository;

    @Override
    public void saveEvent(UUID documentId, String eventType, String traceId, String failureReason) {
        DocumentEventEntity entity = DocumentEventEntity.builder()
                .id(UUID.randomUUID())
                .documentId(documentId)
                .eventType(eventType)
                .traceId(traceId)
                .failureReason(failureReason)
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(entity);
    }

    @Override
    public Map<UUID, EventDetails> findFailureDetailsByDocumentIds(List<UUID> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<DocumentEventEntity> entities = repository.findByDocumentIdInAndEventType(documentIds, "FAILED");

        return entities.stream().collect(Collectors.toMap(
                DocumentEventEntity::getDocumentId,
                entity -> new EventDetails(entity.getTraceId(), entity.getFailureReason()),
                (existing, replacement) -> replacement
        ));
    }

    @Override
    public String findTraceIdByDocumentId(UUID documentId) {
        return repository.findByDocumentIdAndEventType(documentId, "UPLOADED").stream()
                .map(DocumentEventEntity::getTraceId)
                .findFirst()
                .orElse(null);
    }
}
