package com.oussama.sovereignty.infrastructure.adapters.in.messaging;

import com.oussama.sovereignty.application.ports.in.IndexDocumentUseCase;
import com.oussama.sovereignty.application.ports.in.ManageDocumentStatusUseCase;
import com.oussama.sovereignty.domain.model.Document;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

class DocumentWorkerTest {

    private final IndexDocumentUseCase indexDocumentUseCase = mock(IndexDocumentUseCase.class);
    private final ManageDocumentStatusUseCase manageDocumentStatusUseCase = mock(ManageDocumentStatusUseCase.class);
    private final DocumentWorker documentWorker = new DocumentWorker(indexDocumentUseCase, manageDocumentStatusUseCase);

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

    @Test
    void handleDlt_marksDocumentAsFailed() {
        Document document = new Document(
                UUID.randomUUID(),
                "hello.txt",
                "text/plain",
                Document.DocumentStatus.PROCESSING,
                LocalDateTime.now()
        );
        String errorMsg = "Embedding failed";

        documentWorker.handleDlt(document, errorMsg);

        verify(manageDocumentStatusUseCase).updateDocumentStatus(
                document, Document.DocumentStatus.FAILED, null, errorMsg
        );
    }
}