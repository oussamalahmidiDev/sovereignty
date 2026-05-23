package com.oussama.sovereignty.infrastructure.adapters.out.sse;

import com.oussama.sovereignty.application.common.DocumentStatusCallback;
import com.oussama.sovereignty.application.common.Subscription;
import com.oussama.sovereignty.application.ports.out.DocumentStatusNotificationPort;
import com.oussama.sovereignty.domain.model.Document;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class DocumentStatusNotificationAdapter implements DocumentStatusNotificationPort {

    private final Map<UUID, List<DocumentStatusCallback>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void notifyStatusChanged(Document document, String traceId, String failureReason) {
        UUID documentId = document.id();
        List<DocumentStatusCallback> list = subscribers.get(documentId);

        if (list != null && !list.isEmpty()) {
            String dataPayload;
            if (document.status() == Document.DocumentStatus.FAILED) {
                String escapedReason = failureReason != null ? failureReason.replace("\"", "\\\"").replace("\n", " ").replace("\r", " ") : "";
                String safeTraceId = traceId != null ? traceId : "";
                dataPayload = """
                        {"status":"%s","traceId":"%s","failureReason":"%s"}"""
                        .formatted(document.status().name(), safeTraceId, escapedReason);
            } else {
                dataPayload = document.status().name();
            }

            for (DocumentStatusCallback callback : list) {
                try {
                    callback.onStatusChanged(dataPayload);
                    if (document.status() == Document.DocumentStatus.READY) {
                        callback.onComplete();
                    } else if (document.status() == Document.DocumentStatus.FAILED) {
                        callback.onError(new RuntimeException("Document processing failed: " + failureReason));
                    }
                } catch (Exception e) {
                    log.error("Error triggering status update callback for document {}", documentId, e);
                }
            }

            // Cleanup subscription on terminal states (READY or FAILED)
            if (document.status() == Document.DocumentStatus.READY || document.status() == Document.DocumentStatus.FAILED) {
                subscribers.remove(documentId);
            }
        }
    }

    @Override
    public Subscription subscribe(UUID documentId, DocumentStatusCallback callback) {
        subscribers.computeIfAbsent(documentId, k -> new CopyOnWriteArrayList<>()).add(callback);
        log.info("Client subscribed to status updates for document {}", documentId);

        return () -> {
            List<DocumentStatusCallback> list = subscribers.get(documentId);
            if (list != null) {
                list.remove(callback);
                if (list.isEmpty()) {
                    subscribers.remove(documentId);
                }
                log.info("Client unsubscribed from status updates for document {}", documentId);
            }
        };
    }
}
