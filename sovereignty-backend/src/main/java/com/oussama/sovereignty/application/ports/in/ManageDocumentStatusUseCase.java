package com.oussama.sovereignty.application.ports.in;

import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.domain.model.Document.DocumentStatus;

public interface ManageDocumentStatusUseCase {
    void updateDocumentStatus(Document document, DocumentStatus newStatus);
}
