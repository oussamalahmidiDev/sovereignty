package com.oussama.sovereignty.infrastructure.adapters.in.scheduler;

import com.oussama.sovereignty.application.ports.out.DocumentEventPublisherPort;
import com.oussama.sovereignty.application.ports.out.JsonSerializerPort;
import com.oussama.sovereignty.application.ports.out.OutboxRepositoryPort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.domain.model.OutboxEvent;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class OutboxSchedulerTest {

    private final OutboxRepositoryPort outboxRepository = mock(OutboxRepositoryPort.class);
    private final JsonSerializerPort jsonSerializer = mock(JsonSerializerPort.class);
    private final DocumentEventPublisherPort eventPublisher = mock(DocumentEventPublisherPort.class);

    private final OutboxScheduler outboxScheduler = new OutboxScheduler(
            outboxRepository,
            jsonSerializer,
            eventPublisher
    );

    @Test
    void processOutboxEvents_doesNothing_whenNoEvents() {
        when(outboxRepository.findPendingEvents()).thenReturn(Collections.emptyList());

        outboxScheduler.processOutboxEvents();

        verify(outboxRepository).findPendingEvents();
        verifyNoInteractions(jsonSerializer, eventPublisher);
    }

    @Test
    void processOutboxEvents_publishesAndUpdatesStatus_forDocumentUploadedEvent() {
        UUID eventId = UUID.randomUUID();
        UUID docId = UUID.randomUUID();
        String payload = "{}";
        OutboxEvent event = new OutboxEvent(
                eventId,
                "DOCUMENT",
                docId.toString(),
                "DOCUMENT_UPLOADED",
                payload,
                "trace-123",
                OutboxEvent.OutboxStatus.PENDING,
                LocalDateTime.now(),
                null
        );

        Document document = new Document(docId, "file.txt", "text/plain", Document.DocumentStatus.UPLOADED, LocalDateTime.now());

        when(outboxRepository.findPendingEvents()).thenReturn(List.of(event));
        when(jsonSerializer.deserialize(payload, Document.class)).thenReturn(document);

        outboxScheduler.processOutboxEvents();

        verify(eventPublisher).publishDocumentUploaded(document);
        verify(outboxRepository).updateStatus(eq(eventId), eq(OutboxEvent.OutboxStatus.PROCESSED), any(LocalDateTime.class));
    }

    @Test
    void processOutboxEvents_marksFailed_forUnknownEventType() {
        UUID eventId = UUID.randomUUID();
        OutboxEvent event = new OutboxEvent(
                eventId,
                "DOCUMENT",
                "123",
                "UNKNOWN_EVENT",
                "{}",
                "trace-123",
                OutboxEvent.OutboxStatus.PENDING,
                LocalDateTime.now(),
                null
        );

        when(outboxRepository.findPendingEvents()).thenReturn(List.of(event));

        outboxScheduler.processOutboxEvents();

        verifyNoInteractions(eventPublisher, jsonSerializer);
        verify(outboxRepository).updateStatus(eq(eventId), eq(OutboxEvent.OutboxStatus.FAILED), any(LocalDateTime.class));
    }
}
