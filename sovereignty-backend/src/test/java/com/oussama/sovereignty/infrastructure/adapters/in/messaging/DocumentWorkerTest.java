package com.oussama.sovereignty.infrastructure.adapters.in.messaging;

import com.oussama.sovereignty.application.ports.in.ManageDocumentStatusUseCase;
import com.oussama.sovereignty.application.ports.out.DocumentStoragePort;
import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.infrastructure.adapters.out.parsers.TextDocumentParserAdapter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DocumentWorkerTest {


    private final DocumentWorker documentWorker;

    private final DocumentStoragePort documentStoragePort;
    private final TextDocumentParserAdapter documentParserPort;
    private final VectorStorePort vectorStorePort;
    private final ManageDocumentStatusUseCase manageDocumentStatusUseCase;

    DocumentWorkerTest() {
        this.documentStoragePort = mock(DocumentStoragePort.class);
        this.documentParserPort = mock(TextDocumentParserAdapter.class);
        this.vectorStorePort = mock(VectorStorePort.class);
        this.manageDocumentStatusUseCase = mock(ManageDocumentStatusUseCase.class);

        this.documentWorker = new DocumentWorker(
                documentStoragePort,
                List.of(documentParserPort),
                documentParserPort,
                vectorStorePort,
                manageDocumentStatusUseCase,
                null
        );
    }

    @Test
    void processDocument() {

        when(documentParserPort.parse(any())).thenReturn("Hello\nWorld");

        doNothing().when(manageDocumentStatusUseCase).updateDocumentStatus(any(), any());

        Document document = new Document(
                UUID.randomUUID(),
                "hello",
                "txt",
                Document.DocumentStatus.UPLOADED,
                LocalDateTime.now()
        );
        documentWorker.processDocument(document);

        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> chunkCaptor = ArgumentCaptor.forClass(String.class);

        verify(documentStoragePort, atLeastOnce()).load(anyString());
        verify(vectorStorePort).embed(idCaptor.capture(), chunkCaptor.capture());

        assertEquals(document.id(), idCaptor.getValue());
        assertEquals("Hello\nWorld", chunkCaptor.getValue());
    }

    @Test
    void processDocument_Failure() {
        when(documentStoragePort.load(anyString())).thenThrow(new RuntimeException("Simulated processing error"));

        Document document = new Document(
                UUID.randomUUID(),
                "test-error.txt",
                "txt",
                Document.DocumentStatus.UPLOADED,
                LocalDateTime.now()
        );
        documentWorker.processDocument(document);

        verify(manageDocumentStatusUseCase).updateDocumentStatus(
                eq(document),
                eq(Document.DocumentStatus.FAILED),
                isNull(),
                eq("Simulated processing error")
        );
    }
}