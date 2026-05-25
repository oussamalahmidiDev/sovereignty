package com.oussama.sovereignty.infrastructure.adapters.out.messaging;

import com.oussama.sovereignty.application.ports.out.DocumentEventPublisherPort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.application.ports.in.IndexDocumentUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "sovereignty.messaging.provider", havingValue = "internal", matchIfMissing = true)
public class InternalDocumentPublisherAdapter implements DocumentEventPublisherPort {

    private final IndexDocumentUseCase indexDocumentUseCase;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void publishDocumentUploaded(Document document) {
        log.info("Received internal event for document: {}. Processing asynchronously on virtual thread.", document.fileName());
        executor.submit(() -> {
            try {
                indexDocumentUseCase.indexDocument(document);
            } catch (Exception e) {
                log.error("Failed to process document internally: {}", document.fileName(), e);
            }
        });
    }
}
