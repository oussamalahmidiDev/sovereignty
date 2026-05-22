package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.ports.out.DocumentEventRepositoryPort;
import com.oussama.sovereignty.application.ports.out.DocumentRepositoryPort;
import com.oussama.sovereignty.application.ports.out.DocumentStatusNotificationPort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.domain.model.Document.DocumentStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DocumentStatusServiceTest {

    private final DocumentRepositoryPort documentRepositoryPort = mock(DocumentRepositoryPort.class);
    private final DocumentStatusNotificationPort documentStatusNotificationPort = mock(DocumentStatusNotificationPort.class);
    private final DocumentEventRepositoryPort documentEventRepositoryPort = mock(DocumentEventRepositoryPort.class);
    private final DocumentStatusService documentStatusService = new DocumentStatusService(
            documentRepositoryPort,
            documentStatusNotificationPort,
            documentEventRepositoryPort
    );

    @Test
    void updateDocumentStatus_withTraceId() {
        Document document = new Document(UUID.randomUUID(), "test.txt", "txt", DocumentStatus.UPLOADED, LocalDateTime.now());
        
        documentStatusService.updateDocumentStatus(document, DocumentStatus.FAILED, "trace-123", "Some error");
        
        verify(documentRepositoryPort).save(any(Document.class));
        verify(documentEventRepositoryPort).saveEvent(document.id(), "FAILED", "trace-123", "Some error");
        verify(documentStatusNotificationPort).notifyStatusChanged(any(Document.class), eq("trace-123"), eq("Some error"));
    }

    @Test
    void updateDocumentStatus_withoutTraceId_resolvesFromDatabase() {
        Document document = new Document(UUID.randomUUID(), "test.txt", "txt", DocumentStatus.UPLOADED, LocalDateTime.now());
        
        when(documentEventRepositoryPort.findTraceIdByDocumentId(document.id())).thenReturn("original-trace-id");
        
        documentStatusService.updateDocumentStatus(document, DocumentStatus.FAILED, null, "Some error");
        
        verify(documentRepositoryPort).save(any(Document.class));
        verify(documentEventRepositoryPort).saveEvent(document.id(), "FAILED", "original-trace-id", "Some error");
        verify(documentStatusNotificationPort).notifyStatusChanged(any(Document.class), eq("original-trace-id"), eq("Some error"));
    }

    @Test
    void updateDocumentStatus_withoutTraceIdOrUploadedEvent_defaultsToNA() {
        Document document = new Document(UUID.randomUUID(), "test.txt", "txt", DocumentStatus.UPLOADED, LocalDateTime.now());
        
        when(documentEventRepositoryPort.findTraceIdByDocumentId(document.id())).thenReturn(null);
        
        documentStatusService.updateDocumentStatus(document, DocumentStatus.FAILED, null, "Some error");
        
        verify(documentRepositoryPort).save(any(Document.class));
        verify(documentEventRepositoryPort).saveEvent(document.id(), "FAILED", "N/A", "Some error");
        verify(documentStatusNotificationPort).notifyStatusChanged(any(Document.class), eq("N/A"), eq("Some error"));
    }
}
