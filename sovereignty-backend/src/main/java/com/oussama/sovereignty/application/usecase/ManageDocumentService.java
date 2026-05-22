package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.ports.in.ManageDocumentUseCase;
import com.oussama.sovereignty.application.ports.out.DocumentEventPublisherPort;
import com.oussama.sovereignty.application.ports.out.DocumentEventRepositoryPort;
import com.oussama.sovereignty.application.ports.out.DocumentRepositoryPort;
import com.oussama.sovereignty.application.ports.out.DocumentStoragePort;
import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.application.aop.TimedStep;
import com.oussama.sovereignty.application.ports.out.TraceContextPort;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ManageDocumentService implements ManageDocumentUseCase {

    private final DocumentRepositoryPort documentRepository;
    private final DocumentStoragePort storagePort;
    private final DocumentEventPublisherPort eventPublisher;
    private final VectorStorePort vectorStorePort;
    private final DocumentEventRepositoryPort documentEventRepository;
    private final TraceContextPort traceContextPort;

    @Override
    public void importDocument(String fileName, String contentType, byte[] content) {

        Document document = new Document(
                UUID.randomUUID(),
                fileName,
                contentType,
                Document.DocumentStatus.UPLOADED,
                LocalDateTime.now()
        );

        storagePort.store(content, fileName);

        documentRepository.save(document);

        String traceId = traceContextPort.getCurrentTraceId();
        documentEventRepository.saveEvent(document.id(), "UPLOADED", traceId, null);

        eventPublisher.publishDocumentUploaded(document);
    }

    @Override
    @TimedStep("rag.findalldocs")
    public List<Document> findAllDocuments() {
        return documentRepository.findAllDocuments();
    }

    @Override
    public void deleteDocument(UUID id, String fileName) {
        documentRepository.deleteDocument(id);

        storagePort.delete(fileName);

        vectorStorePort.clean(id);
    }
}
