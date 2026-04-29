package com.oussama.sovereignty.infrastructure.adapters.in.messaging;

import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import com.oussama.sovereignty.domain.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentWorker {

    private final VectorStorePort vectorStorePort;
    private final TokenTextSplitter textSplitter = new TokenTextSplitter();


    @KafkaListener(topics = "document-uploaded", groupId = "sovereignty-group")
    public void processDocument(Document document) {
        log.info("Vectorization of file : {}", document.fileName());

        String rawText = new String(document.content());

        var textChunks = textSplitter.split(
                new org.springframework.ai.document.Document(rawText, Map.of("documentId", document.id().toString()))
        );
        log.info("Document is splitted in {} chunks.", textChunks.size());

        for (var chunk : textChunks) {
            vectorStorePort.save(document.id(), chunk.getText());
        }

        log.info("Vectorisation is finished successfully : {}", document.fileName());
    }
}
