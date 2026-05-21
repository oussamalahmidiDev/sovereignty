package com.oussama.sovereignty.infrastructure.adapters.in.messaging;

import com.oussama.sovereignty.application.ports.in.ManageDocumentStatusUseCase;
import com.oussama.sovereignty.application.ports.out.DocumentParserPort;
import com.oussama.sovereignty.application.ports.out.DocumentRepositoryPort;
import com.oussama.sovereignty.application.ports.out.DocumentStoragePort;
import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import com.oussama.sovereignty.application.usecase.DocumentStatusService;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.domain.model.Document.DocumentStatus;
import com.oussama.sovereignty.infrastructure.adapters.out.parsers.TextDocumentParserAdapter;
import com.oussama.sovereignty.infrastructure.aop.TimedStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.oussama.sovereignty.infrastructure.Constants.DOCUMENT_UPLOADED_TOPIC;

@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentWorker {

    private final DocumentStoragePort storagePort;

    private final List<DocumentParserPort> parsers;
    private final TextDocumentParserAdapter defaultParserAdapter;

    private final VectorStorePort vectorStorePort;

    private final ManageDocumentStatusUseCase manageDocumentStatusUseCase;


    private final TokenTextSplitter textSplitter = new TokenTextSplitter();


    @KafkaListener(topics = DOCUMENT_UPLOADED_TOPIC, groupId = "sovereignty-group")
    @TimedStep("rag.embed.duration")
    public void processDocument(Document document) {
        try {
            log.info("Vectorization of file : {}", document.fileName());

            // mark as processing
            manageDocumentStatusUseCase.updateDocumentStatus(document, DocumentStatus.PROCESSING);

            byte[] content = storagePort.load(document.fileName());
            String extension = document.fileName().substring(document.fileName().lastIndexOf(".") + 1);

            DocumentParserPort parser = parsers.stream()
                    .filter(documentParserPort -> documentParserPort.supports(extension))
                    .findFirst()
                    .orElse(defaultParserAdapter);

            // Extract content from the file.
            String rawText = parser.parse(content);

            // Split the content into small chunks
            var textChunks = textSplitter.split(
                    new org.springframework.ai.document.Document(rawText, Map.of("documentId", document.id().toString()))
            );
            log.info("Document is splitted in {} chunks.", textChunks.size());

            // Here where vectorization happens, the implementation it will call the embedding model
            for (var chunk : textChunks) {
                vectorStorePort.embed(document.id(), chunk.getText());
            }

            // mark as ready
            manageDocumentStatusUseCase.updateDocumentStatus(document, DocumentStatus.READY);

            log.info("Vectorisation is finished successfully : {}", document.fileName());
        } catch (Exception ex) {
            log.error("Error while processing document {}", document.fileName(), ex);

            manageDocumentStatusUseCase.updateDocumentStatus(document, DocumentStatus.FAILED);
        }

    }
}
