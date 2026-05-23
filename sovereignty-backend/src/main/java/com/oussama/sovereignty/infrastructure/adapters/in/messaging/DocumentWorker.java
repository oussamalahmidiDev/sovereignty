package com.oussama.sovereignty.infrastructure.adapters.in.messaging;

import com.oussama.sovereignty.application.ports.in.IndexDocumentUseCase;
import com.oussama.sovereignty.domain.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.oussama.sovereignty.infrastructure.Constants.DOCUMENT_UPLOADED_TOPIC;

@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentWorker {

    private final IndexDocumentUseCase indexDocumentUseCase;

    @KafkaListener(topics = DOCUMENT_UPLOADED_TOPIC, groupId = "sovereignty-group")
    public void processDocument(Document document) {
        log.info("Received Kafka event for document: {}", document.fileName());
        indexDocumentUseCase.indexDocument(document);
    }
}
