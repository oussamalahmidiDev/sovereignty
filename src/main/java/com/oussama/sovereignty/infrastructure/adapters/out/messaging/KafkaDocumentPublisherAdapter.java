package com.oussama.sovereignty.infrastructure.adapters.out.messaging;

import com.oussama.sovereignty.application.ports.out.DocumentEventPublisherPort;
import com.oussama.sovereignty.domain.model.Document;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaDocumentPublisherAdapter implements DocumentEventPublisherPort {

    private static final String TOPIC = "document-uploaded";


    private final KafkaTemplate<String, Document> kafkaTemplate;

    @Override
    public void publishDocumentUploaded(Document document) {
        kafkaTemplate.send(TOPIC, document.id().toString(), document);
    }
}
