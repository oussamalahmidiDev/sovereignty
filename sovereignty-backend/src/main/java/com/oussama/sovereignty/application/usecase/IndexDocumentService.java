package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.ports.in.IndexDocumentUseCase;
import com.oussama.sovereignty.application.ports.in.ManageDocumentStatusUseCase;
import com.oussama.sovereignty.application.ports.out.DocumentParserPort;
import com.oussama.sovereignty.application.ports.out.DocumentStoragePort;
import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import com.oussama.sovereignty.application.ports.out.TraceContextPort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.domain.model.Document.DocumentStatus;
import com.oussama.sovereignty.application.common.UseCase;
import com.oussama.sovereignty.application.aop.TimedStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

@UseCase
@Slf4j
@RequiredArgsConstructor
public class IndexDocumentService implements IndexDocumentUseCase {

    private final DocumentStoragePort storagePort;
    private final List<DocumentParserPort> parsers;
    private final DocumentParserPort defaultParserAdapter; // Inject the default TextDocumentParserAdapter bean
    private final VectorStorePort vectorStorePort;
    private final ManageDocumentStatusUseCase manageDocumentStatusUseCase;
    private final TraceContextPort traceContextPort;

    private final TokenTextSplitter textSplitter = new TokenTextSplitter(
            400, 350, 10, 10000, true, List.of('.', ',', '?', '!', '\n')
    );

    @Override
    @TimedStep("rag.embed.duration")
    public void indexDocument(Document document) {
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
                    new org.springframework.ai.document.Document(
                            rawText,
                            Map.of("documentId", document.id().toString())
                    )
            );
            log.info("Document is split into {} chunks.", textChunks.size());

            // Vectorization and storage in parallel using virtual threads (Structured Concurrency style)
            List<Callable<Void>> tasks = textChunks.stream()
                    .map(chunk -> (Callable<Void>) () -> {
                        vectorStorePort.embed(document.id(), chunk.getText());
                        return null;
                    })
                    .toList();

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                executor.invokeAll(tasks);
            }

            // mark as ready
            manageDocumentStatusUseCase.updateDocumentStatus(document, DocumentStatus.READY);

            log.info("Vectorisation is finished successfully : {}", document.fileName());
        } catch (Exception ex) {
            log.error("Error while processing document {}", document.fileName(), ex);

            String traceId = traceContextPort.getCurrentTraceId();
            String failureReason = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();

            manageDocumentStatusUseCase.updateDocumentStatus(document, DocumentStatus.FAILED, traceId, failureReason);
        }
    }
}
