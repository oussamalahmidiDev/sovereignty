package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.ports.in.ManageDocumentStatusUseCase;
import com.oussama.sovereignty.application.ports.out.DocumentEventRepositoryPort;
import com.oussama.sovereignty.application.ports.out.DocumentRepositoryPort;
import com.oussama.sovereignty.application.ports.out.DocumentStatusNotificationPort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.domain.model.Document.DocumentStatus;
import com.oussama.sovereignty.application.common.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@UseCase
@Slf4j
@RequiredArgsConstructor
public class DocumentStatusService implements ManageDocumentStatusUseCase {


    private final DocumentRepositoryPort documentRepositoryPort;
    private final DocumentStatusNotificationPort documentStatusNotificationPort;
    private final DocumentEventRepositoryPort documentEventRepositoryPort;

    @Override
    public void updateDocumentStatus(Document document, DocumentStatus newStatus) {
        updateDocumentStatus(document, newStatus, null, null);
    }

    @Override
    public void updateDocumentStatus(Document document, DocumentStatus newStatus, String traceId, String failureReason) {
        Document updatedDocument = document.copyAndChangeStatus(newStatus);
        documentRepositoryPort.save(updatedDocument);
        
        String resolvedTraceId = traceId;
        if (resolvedTraceId == null) {
            resolvedTraceId = documentEventRepositoryPort.findTraceIdByDocumentId(document.id());
        }
        if (resolvedTraceId == null) {
            resolvedTraceId = "N/A";
        }
        
        documentEventRepositoryPort.saveEvent(document.id(), newStatus.name(), resolvedTraceId, failureReason);
        
        documentStatusNotificationPort.notifyStatusChanged(updatedDocument, resolvedTraceId, failureReason);
    }
}