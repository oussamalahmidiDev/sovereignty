package com.oussama.sovereignty.infrastructure.adapters.in.messaging;

import com.oussama.sovereignty.application.ports.in.IndexDocumentUseCase;
import com.oussama.sovereignty.application.ports.in.ManageDocumentStatusUseCase;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.domain.model.Document.DocumentStatus;
import com.oussama.sovereignty.domain.exception.TransientDocumentProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import static com.oussama.sovereignty.infrastructure.Constants.DOCUMENT_UPLOADED_TOPIC;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "sovereignty.messaging.provider", havingValue = "kafka")
public class DocumentWorker {

    private final IndexDocumentUseCase indexDocumentUseCase;
    private final ManageDocumentStatusUseCase manageDocumentStatusUseCase;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000, multiplier = 2.0, maxDelay = 10000),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            include = { TransientDocumentProcessingException.class }
    )
    @KafkaListener(topics = DOCUMENT_UPLOADED_TOPIC, groupId = "sovereignty-group")
    public void processDocument(Document document) {
        log.info("Received Kafka event for document: {}", document.fileName());
        indexDocumentUseCase.indexDocument(document);
    }

    @DltHandler
    public void handleDlt(Document document, @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage) {
        log.error("Document {} failed final retry in DLT. Error: {}", document.fileName(), errorMessage);
        manageDocumentStatusUseCase.updateDocumentStatus(document, DocumentStatus.FAILED, null, errorMessage);
    }
}
