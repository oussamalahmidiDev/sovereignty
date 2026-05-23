package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.ports.in.ManageDocumentStatusUseCase;
import com.oussama.sovereignty.application.ports.out.DocumentParserPort;
import com.oussama.sovereignty.application.ports.out.DocumentStoragePort;
import com.oussama.sovereignty.application.ports.out.TraceContextPort;
import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import com.oussama.sovereignty.domain.model.Document;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IndexDocumentServiceTest {

    private final DocumentStoragePort storagePort = mock(DocumentStoragePort.class);
    private final DocumentParserPort parserPort = mock(DocumentParserPort.class);
    private final VectorStorePort vectorStorePort = mock(VectorStorePort.class);
    private final ManageDocumentStatusUseCase manageDocumentStatusUseCase = mock(ManageDocumentStatusUseCase.class);
    private final TraceContextPort traceContextPort = mock(TraceContextPort.class);

    private final IndexDocumentService indexDocumentService = new IndexDocumentService(
            storagePort,
            List.of(parserPort),
            parserPort,
            vectorStorePort,
            manageDocumentStatusUseCase,
            traceContextPort
    );

    @Test
    void indexDocument_success() {
        UUID docId = UUID.randomUUID();
        Document document = new Document(
                docId,
                "hello.txt",
                "text/plain",
                Document.DocumentStatus.UPLOADED,
                LocalDateTime.now()
        );

        byte[] content = "Hello World".getBytes();
        when(storagePort.load("hello.txt")).thenReturn(content);
        when(parserPort.supports("txt")).thenReturn(true);
        when(parserPort.parse(content)).thenReturn("Hello World");
        when(traceContextPort.getCurrentTraceId()).thenReturn("test-trace-id");

        indexDocumentService.indexDocument(document);

        verify(manageDocumentStatusUseCase).updateDocumentStatus(document, Document.DocumentStatus.PROCESSING, "test-trace-id", null);
        verify(vectorStorePort).embed(eq(docId), anyList());
        verify(manageDocumentStatusUseCase).updateDocumentStatus(document, Document.DocumentStatus.READY, "test-trace-id", null);
    }

    @Test
    void indexDocument_fatalError_marksFailedImmediately() {
        UUID docId = UUID.randomUUID();
        Document document = new Document(
                docId,
                "hello.txt",
                "text/plain",
                Document.DocumentStatus.UPLOADED,
                LocalDateTime.now()
        );

        when(storagePort.load("hello.txt")).thenThrow(new RuntimeException("Storage disk full or missing file"));
        when(traceContextPort.getCurrentTraceId()).thenReturn("test-trace-id");

        indexDocumentService.indexDocument(document);

        verify(manageDocumentStatusUseCase).updateDocumentStatus(document, Document.DocumentStatus.PROCESSING, "test-trace-id", null);
        // Should mark as FAILED directly due to storage failure being wrapped as Fatal
        verify(manageDocumentStatusUseCase).updateDocumentStatus(
                eq(document),
                eq(Document.DocumentStatus.FAILED),
                eq("test-trace-id"),
                contains("Failed to load document content from storage")
        );
        // Vector store should never be invoked
        verifyNoInteractions(vectorStorePort);
    }

    @Test
    void indexDocument_transientError_propagatesException() {
        UUID docId = UUID.randomUUID();
        Document document = new Document(
                docId,
                "hello.txt",
                "text/plain",
                Document.DocumentStatus.UPLOADED,
                LocalDateTime.now()
        );

        byte[] content = "Hello World".getBytes();
        when(storagePort.load("hello.txt")).thenReturn(content);
        when(parserPort.supports("txt")).thenReturn(true);
        when(parserPort.parse(content)).thenReturn("Hello World");
        when(traceContextPort.getCurrentTraceId()).thenReturn("test-trace-id");
        doThrow(new RuntimeException("Ollama timeout")).when(vectorStorePort).embed(eq(docId), anyList());

        org.junit.jupiter.api.Assertions.assertThrows(
                com.oussama.sovereignty.domain.exception.TransientDocumentProcessingException.class,
                () -> indexDocumentService.indexDocument(document)
        );

        verify(manageDocumentStatusUseCase).updateDocumentStatus(document, Document.DocumentStatus.PROCESSING, "test-trace-id", null);
        // Vector store was tried and failed
        verify(vectorStorePort).embed(eq(docId), anyList());
        // Status should NOT be marked as FAILED yet because it's propagated for retry
        verify(manageDocumentStatusUseCase, never()).updateDocumentStatus(
                eq(document),
                eq(Document.DocumentStatus.FAILED),
                any(),
                any()
        );
    }
}
