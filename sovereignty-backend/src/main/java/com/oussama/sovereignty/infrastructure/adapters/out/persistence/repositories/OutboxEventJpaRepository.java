package com.oussama.sovereignty.infrastructure.adapters.out.persistence.repositories;

import com.oussama.sovereignty.domain.model.OutboxEvent.OutboxStatus;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {
    List<OutboxEventEntity> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
