package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.ports.in.ManageDocumentStatusUseCase;
import com.oussama.sovereignty.application.ports.out.DocumentRepositoryPort;
import com.oussama.sovereignty.application.ports.out.DocumentStatusNotificationPort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.domain.model.Document.DocumentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentStatusService implements ManageDocumentStatusUseCase {


    private final DocumentRepositoryPort documentRepositoryPort;
    private final DocumentStatusNotificationPort documentStatusNotificationPort;

    @Override
    public void updateDocumentStatus(Document document, DocumentStatus newStatus) {
        Document updatedDocument = document.copyAndChangeStatus(newStatus);
        documentRepositoryPort.save(updatedDocument);
        documentStatusNotificationPort.notifyStatusChanged(updatedDocument);
    }
}