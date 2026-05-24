package com.oussama.sovereignty.infrastructure.adapters.out.persistence;

import com.oussama.sovereignty.application.ports.out.OutboxRepositoryPort;
import com.oussama.sovereignty.domain.model.OutboxEvent;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities.OutboxEventEntity;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.mappers.OutboxEventEntityMapper;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.repositories.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OutboxPersistenceAdapter implements OutboxRepositoryPort {

    private final OutboxEventJpaRepository repository;

    @Override
    @Transactional
    public void save(OutboxEvent event) {
        OutboxEventEntity entity = OutboxEventEntityMapper.toEntity(event);
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> findPendingEvents() {
        return repository.findByStatusOrderByCreatedAtAsc(OutboxEvent.OutboxStatus.PENDING)
                .stream()
                .map(OutboxEventEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(UUID eventId, OutboxEvent.OutboxStatus status, LocalDateTime processedAt) {
        repository.findById(eventId).ifPresent(entity -> {
            entity.setStatus(status);
            entity.setProcessedAt(processedAt);
            repository.save(entity);
        });
    }
}
