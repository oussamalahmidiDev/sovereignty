package com.oussama.sovereignty.infrastructure.adapters.in.messaging;

import com.oussama.sovereignty.application.ports.in.IndexDocumentUseCase;
import com.oussama.sovereignty.domain.model.Document;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

class DocumentWorkerTest {

    private final IndexDocumentUseCase indexDocumentUseCase = mock(IndexDocumentUseCase.class);
    private final DocumentWorker documentWorker = new DocumentWorker(indexDocumentUseCase);

    @Test
    void processDocument_delegatesToIndexDocumentUseCase() {
        Document document = new Document(
                UUID.randomUUID(),
                "hello.txt",
                "text/plain",
                Document.DocumentStatus.UPLOADED,
                LocalDateTime.now()
        );

        documentWorker.processDocument(document);

        verify(indexDocumentUseCase).indexDocument(document);
    }
}