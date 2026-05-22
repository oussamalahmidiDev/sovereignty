package com.oussama.sovereignty.infrastructure.adapters.out.messaging;

import com.oussama.sovereignty.application.ports.out.DocumentEventPublisherPort;
import com.oussama.sovereignty.domain.model.Document;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.oussama.sovereignty.infrastructure.Constants.DOCUMENT_UPLOADED_TOPIC;

@Component
@RequiredArgsConstructor
public class KafkaDocumentPublisherAdapter implements DocumentEventPublisherPort {

    private final KafkaTemplate<String, Document> kafkaTemplate;

    @Override
    public void publishDocumentUploaded(Document document) {
        kafkaTemplate.send(DOCUMENT_UPLOADED_TOPIC, document.id().toString(), document);
    }
}
