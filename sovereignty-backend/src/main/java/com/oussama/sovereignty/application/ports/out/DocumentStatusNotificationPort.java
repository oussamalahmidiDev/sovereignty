package com.oussama.sovereignty.application.ports.out;

import com.oussama.sovereignty.application.common.DocumentStatusCallback;
import com.oussama.sovereignty.application.common.Subscription;
import com.oussama.sovereignty.domain.model.Document;

import java.util.UUID;

public interface DocumentStatusNotificationPort {
    void notifyStatusChanged(Document document, String traceId, String failureReason);
    Subscription subscribe(UUID documentId, DocumentStatusCallback callback);
}
