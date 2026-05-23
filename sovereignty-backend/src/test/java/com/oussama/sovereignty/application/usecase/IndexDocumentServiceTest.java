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

        indexDocumentService.indexDocument(document);

        verify(manageDocumentStatusUseCase).updateDocumentStatus(document, Document.DocumentStatus.PROCESSING);
        verify(vectorStorePort, atLeastOnce()).embed(eq(docId), anyString());
        verify(manageDocumentStatusUseCase).updateDocumentStatus(document, Document.DocumentStatus.READY);
    }
}
