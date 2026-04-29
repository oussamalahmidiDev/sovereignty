package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.ports.in.ImportDocumentUseCase;
import com.oussama.sovereignty.application.ports.out.DocumentEventPublisherPort;
import com.oussama.sovereignty.application.ports.out.DocumentRepositoryPort;
import com.oussama.sovereignty.application.ports.out.DocumentStoragePort;
import com.oussama.sovereignty.domain.model.Document;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImportDocumentService implements ImportDocumentUseCase {

    private final DocumentRepositoryPort documentRepository;
    private final DocumentStoragePort storagePort;
    private final DocumentEventPublisherPort eventPublisher;

    @Override
    @Transactional
    public void importDocument(String fileName, String contentType, byte[] content) {

        Document document = new Document(
                UUID.randomUUID(),
                fileName,
                contentType,
                Document.DocumentStatus.UPLOADED,
                content,
                LocalDateTime.now()
        );

        storagePort.store(content, fileName);

        documentRepository.save(document);

        eventPublisher.publishDocumentUploaded(document);
    }
}
