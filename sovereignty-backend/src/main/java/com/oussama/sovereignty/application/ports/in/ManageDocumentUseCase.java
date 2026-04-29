package com.oussama.sovereignty.application.ports.in;

import com.oussama.sovereignty.domain.model.Document;

import java.util.List;
import java.util.UUID;

public interface ManageDocumentUseCase {
    void importDocument(String fileName, String contentType, byte[] content);

    List<Document> findAllDocuments();
    void deleteDocument(UUID id, String fileName);
}
