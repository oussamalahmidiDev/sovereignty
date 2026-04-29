package com.oussama.sovereignty.infrastructure.adapters.in.messaging;

import com.oussama.sovereignty.application.ports.out.AiAgentPort;
import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.domain.service.TextSplitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentWorker {

    private final AiAgentPort aiAgentPort;
    private final VectorStorePort vectorStorePort;
    private final TextSplitter textSplitter = new TextSplitter(1000, 200);


    @KafkaListener(topics = "document-uploaded", groupId = "sovereignty-group")
    public void processDocument(Document document) {
        log.info("Vectorization of file : {}", document.fileName());

        String rawText = new String(document.content());

        List<String> textChunks = textSplitter.split(rawText);
        log.info("Document is splitted in {} morceaux.", textChunks.size());

        for (String chunk : textChunks) {
            float[] vector = aiAgentPort.embed(chunk);

            vectorStorePort.save(document.id(), chunk, vector);
        }

        log.info("Vectorisation is finished successfully : {}", document.fileName());
    }
}
