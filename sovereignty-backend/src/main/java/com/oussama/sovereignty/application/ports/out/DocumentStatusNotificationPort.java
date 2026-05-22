package com.oussama.sovereignty.application.ports.out;

import com.oussama.sovereignty.domain.model.Document;

public interface DocumentStatusNotificationPort {
    void notifyStatusChanged(Document document, String traceId, String failureReason);
}
