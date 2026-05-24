package com.oussama.sovereignty.application.ports.out;

import com.oussama.sovereignty.domain.model.OutboxEvent;
import java.util.List;
import java.util.UUID;

public interface OutboxRepositoryPort {
    void save(OutboxEvent event);
    List<OutboxEvent> findPendingEvents();
    void updateStatus(UUID eventId, OutboxEvent.OutboxStatus status, java.time.LocalDateTime processedAt);
}
