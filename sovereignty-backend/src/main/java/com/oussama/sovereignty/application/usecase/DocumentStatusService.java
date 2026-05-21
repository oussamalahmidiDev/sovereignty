package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.ports.in.ManageDocumentStatusUseCase;
import com.oussama.sovereignty.application.ports.out.DocumentRepositoryPort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.domain.model.Document.DocumentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentStatusService implements ManageDocumentStatusUseCase {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final DocumentRepositoryPort documentRepositoryPort;

    @Override
    public void updateDocumentStatus(Document document, DocumentStatus newStatus) {
        Document updatedDocument = document.copyAndChangeStatus(newStatus);
        documentRepositoryPort.save(updatedDocument);
        notifyStatusChange(updatedDocument);
    }

    public SseEmitter subscribe(String documentId) {
        SseEmitter emitter = new SseEmitter(60000L); // 60 second timeout

        emitters.put(documentId, emitter);

        emitter.onCompletion(() -> emitters.remove(documentId));
        emitter.onTimeout(() -> emitters.remove(documentId));
        emitter.onError(throwable -> emitters.remove(documentId));

        return emitter;
    }

    private void notifyStatusChange(Document document) {
        String documentId = document.id().toString();
        SseEmitter emitter = emitters.get(documentId);

        if (emitter != null) {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .id(UUID.randomUUID().toString())
                        .name("status-update")
                        .data(document.status().name())
                        .reconnectTime(5000);

                emitter.send(event);
                log.info("Status update sent for document {}: {}", documentId, document.status());
            } catch (IOException e) {
                log.error("Error sending SSE event for document {}", documentId, e);
                emitters.remove(documentId);
            }
        }
    }
}