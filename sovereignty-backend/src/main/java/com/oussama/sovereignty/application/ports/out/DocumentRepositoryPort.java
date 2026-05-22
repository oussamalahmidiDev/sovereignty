package com.oussama.sovereignty.application.ports.out;

import com.oussama.sovereignty.domain.model.Document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepositoryPort {
    void save(Document document);

    List<Document> findAllDocuments();

    Optional<Document> findDocumentById(UUID id);

    void deleteDocument(UUID id);
}
