package com.oussama.sovereignty.application.ports.in;

import com.oussama.sovereignty.application.common.DocumentStatusCallback;
import com.oussama.sovereignty.application.common.Subscription;
import com.oussama.sovereignty.domain.model.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ManageDocumentUseCase {
    void importDocument(String fileName, String contentType, byte[] content);

    List<DocumentDetails> findAllDocuments();
    DownloadedDocument downloadDocument(UUID id);
    void deleteDocument(UUID id, String fileName);

    Subscription subscribeToStatus(UUID documentId, DocumentStatusCallback callback);

    record DocumentDetails(
            UUID id,
            String fileName,
            String contentType,
            String status,
            LocalDateTime createdAt,
            String traceId,
            String failureReason
    ) {}

    record DownloadedDocument(String fileName, String contentType, byte[] content) {
    }
}
