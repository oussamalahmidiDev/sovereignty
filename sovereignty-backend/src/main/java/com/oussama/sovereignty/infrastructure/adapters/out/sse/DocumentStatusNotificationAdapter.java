package com.oussama.sovereignty.infrastructure.adapters.out.sse;

import com.oussama.sovereignty.application.ports.out.DocumentStatusNotificationPort;
import com.oussama.sovereignty.domain.model.Document;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DocumentStatusNotificationAdapter implements DocumentStatusNotificationPort {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();


    @Override
    public void notifyStatusChanged(Document document, String traceId, String failureReason) {
        String documentId = document.id().toString();
        SseEmitter emitter = emitters.get(documentId);

        if (emitter != null) {
            try {
                String dataPayload;
                if (document.status() == Document.DocumentStatus.FAILED) {
                    String escapedReason = failureReason != null ? failureReason.replace("\"", "\\\"").replace("\n", " ").replace("\r", " ") : "";
                    String safeTraceId = traceId != null ? traceId : "";
                    dataPayload = String.format("{\"status\":\"%s\",\"traceId\":\"%s\",\"failureReason\":\"%s\"}", document.status().name(), safeTraceId, escapedReason);
                } else {
                    dataPayload = document.status().name();
                }

                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .id(UUID.randomUUID().toString())
                        .name("status-update")
                        .data(dataPayload)
                        .reconnectTime(5000);

                emitter.send(event);
                log.info("Status update sent for document {}: {}", documentId, document.status());
            } catch (IOException e) {
                log.error("Error sending SSE event for document {}", documentId, e);
                emitters.remove(documentId);
            }
        }
    }

    public SseEmitter subscribe(String documentId) {
        SseEmitter emitter = new SseEmitter(60000L); // 60 second timeout

        emitters.put(documentId, emitter);

        emitter.onCompletion(() -> emitters.remove(documentId));
        emitter.onTimeout(() -> emitters.remove(documentId));
        emitter.onError(throwable -> emitters.remove(documentId));

        return emitter;
    }
}
