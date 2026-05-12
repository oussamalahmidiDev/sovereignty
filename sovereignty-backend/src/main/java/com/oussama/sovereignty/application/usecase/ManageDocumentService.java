package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.ports.in.ManageDocumentUseCase;
import com.oussama.sovereignty.application.ports.out.DocumentEventPublisherPort;
import com.oussama.sovereignty.application.ports.out.DocumentRepositoryPort;
import com.oussama.sovereignty.application.ports.out.DocumentStoragePort;
import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.infrastructure.aop.TimedStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManageDocumentService implements ManageDocumentUseCase {

    private final DocumentRepositoryPort documentRepository;
    private final DocumentStoragePort storagePort;
    private final DocumentEventPublisherPort eventPublisher;
    private final VectorStorePort vectorStorePort;

    @Override
    @Transactional
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

        eventPublisher.publishDocumentUploaded(document);
    }

    @Override
    @TimedStep("rag.findalldocs")
    public List<Document> findAllDocuments() {
        return documentRepository.findAllDocuments();
    }

    @Override
    @Transactional
    public void deleteDocument(UUID id, String fileName) {
        documentRepository.deleteDocument(id);

        storagePort.delete(fileName);

        vectorStorePort.clean(id);
    }
}
