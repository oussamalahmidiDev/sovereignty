package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.ports.out.*;
import com.oussama.sovereignty.application.ports.in.ManageDocumentUseCase.DownloadedDocument;
import com.oussama.sovereignty.domain.model.Document;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ManageDocumentServiceTest {

    private final DocumentRepositoryPort documentRepository = mock(DocumentRepositoryPort.class);
    private final DocumentStoragePort storagePort = mock(DocumentStoragePort.class);
    private final DocumentEventPublisherPort eventPublisher = mock(DocumentEventPublisherPort.class);
    private final VectorStorePort vectorStorePort = mock(VectorStorePort.class);
    private final DocumentEventRepositoryPort documentEventRepository = mock(DocumentEventRepositoryPort.class);
    private final TraceContextPort traceContextPort = mock(TraceContextPort.class);
    private final DocumentStatusNotificationPort documentStatusNotificationPort = mock(DocumentStatusNotificationPort.class);
    private final OutboxRepositoryPort outboxRepository = mock(OutboxRepositoryPort.class);
    private final JsonSerializerPort jsonSerializer = mock(JsonSerializerPort.class);

    private final ManageDocumentService manageDocumentService = new ManageDocumentService(
            documentRepository,
            storagePort,
            eventPublisher,
            vectorStorePort,
            documentEventRepository,
            traceContextPort,
            documentStatusNotificationPort,
            outboxRepository,
            jsonSerializer
    );

    @Test
    void importDocument_storesAndSavesWithTraceId() {
        String fileName = "test.txt";
        String contentType = "text/plain";
        byte[] content = "hello".getBytes();
        String traceId = "test-trace-id-999";

        when(traceContextPort.getCurrentTraceId()).thenReturn(traceId);
        when(jsonSerializer.serialize(any(Document.class))).thenReturn("{\"id\":\"some-uuid\"}");

        manageDocumentService.importDocument(fileName, contentType, content);

        verify(storagePort).store(content, fileName);
        verify(documentRepository).save(any(Document.class));
        verify(documentEventRepository).saveEvent(any(UUID.class), eq("UPLOADED"), eq(traceId), eq(null));
        verify(jsonSerializer).serialize(any(Document.class));
        verify(outboxRepository).save(any(com.oussama.sovereignty.domain.model.OutboxEvent.class));
        verify(eventPublisher, never()).publishDocumentUploaded(any(Document.class));
    }

    @Test
    void findAllDocuments_delegatesToRepository() {
        UUID docId = UUID.randomUUID();
        Document mockDoc = new Document(docId, "test.txt", "text/plain", Document.DocumentStatus.READY, LocalDateTime.now());
        List<Document> mockDocs = List.of(mockDoc);
        when(documentRepository.findAllDocuments()).thenReturn(mockDocs);
        when(documentEventRepository.findFailureDetailsByDocumentIds(anyList())).thenReturn(java.util.Collections.emptyMap());

        var result = manageDocumentService.findAllDocuments();

        assertEquals(1, result.size());
        assertEquals(docId, result.get(0).id());
        assertEquals("test.txt", result.get(0).fileName());
        assertEquals("READY", result.get(0).status());
        verify(documentRepository).findAllDocuments();
        verify(documentEventRepository).findFailureDetailsByDocumentIds(anyList());
    }

    @Test
    void deleteDocument_deletesFromAllStores() {
        UUID docId = UUID.randomUUID();
        String fileName = "test.txt";

        manageDocumentService.deleteDocument(docId, fileName);

        verify(documentRepository).deleteDocument(docId);
        verify(storagePort).delete(fileName);
        verify(vectorStorePort).clean(docId);
    }

    @Test
    void downloadDocument_loadsStoredContentForDocumentId() {
        UUID docId = UUID.randomUUID();
        Document document = new Document(
                docId,
                "test.txt",
                "text/plain",
                Document.DocumentStatus.READY,
                LocalDateTime.now()
        );
        byte[] content = "hello".getBytes();

        when(documentRepository.findDocumentById(docId)).thenReturn(Optional.of(document));
        when(storagePort.load("test.txt")).thenReturn(content);

        DownloadedDocument result = manageDocumentService.downloadDocument(docId);

        assertEquals("test.txt", result.fileName());
        assertEquals("text/plain", result.contentType());
        assertArrayEquals(content, result.content());
        verify(documentRepository).findDocumentById(docId);
        verify(documentRepository, never()).findAllDocuments();
        verify(storagePort).load("test.txt");
    }
}
