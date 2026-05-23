package com.oussama.sovereignty.application.ports.in;

import com.oussama.sovereignty.domain.model.Document;

public interface IndexDocumentUseCase {
    void indexDocument(Document document);
}
