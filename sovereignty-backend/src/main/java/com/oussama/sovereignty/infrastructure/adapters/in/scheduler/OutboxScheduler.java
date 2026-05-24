package com.oussama.sovereignty.infrastructure.adapters.in.scheduler;

import com.oussama.sovereignty.application.ports.out.DocumentEventPublisherPort;
import com.oussama.sovereignty.application.ports.out.JsonSerializerPort;
import com.oussama.sovereignty.application.ports.out.OutboxRepositoryPort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.domain.model.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxRepositoryPort outboxRepository;
    private final JsonSerializerPort jsonSerializer;
    private final DocumentEventPublisherPort eventPublisher;

    @Scheduled(fixedDelay = 1000)
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findPendingEvents();
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events to process", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                if ("DOCUMENT_UPLOADED".equals(event.eventType())) {
                    Document document = jsonSerializer.deserialize(event.payload(), Document.class);
                    eventPublisher.publishDocumentUploaded(document);
                    outboxRepository.updateStatus(event.id(), OutboxEvent.OutboxStatus.PROCESSED, LocalDateTime.now());
                    log.info("Successfully published outbox event: {} for aggregate: {}", event.id(), event.aggregateId());
                } else {
                    log.warn("Unknown event type in outbox: {}", event.eventType());
                    outboxRepository.updateStatus(event.id(), OutboxEvent.OutboxStatus.FAILED, LocalDateTime.now());
                }
            } catch (Exception e) {
                log.error("Failed to process outbox event: {}", event.id(), e);
            }
        }
    }
}
