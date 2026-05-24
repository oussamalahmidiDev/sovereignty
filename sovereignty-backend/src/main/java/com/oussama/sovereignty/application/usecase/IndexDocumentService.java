package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.ports.in.IndexDocumentUseCase;
import com.oussama.sovereignty.application.ports.in.ManageDocumentStatusUseCase;
import com.oussama.sovereignty.application.ports.out.DocumentParserPort;
import com.oussama.sovereignty.application.ports.out.DocumentStoragePort;
import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import com.oussama.sovereignty.application.ports.out.TraceContextPort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.domain.model.Document.DocumentStatus;
import com.oussama.sovereignty.domain.exception.FatalDocumentProcessingException;
import com.oussama.sovereignty.domain.exception.TransientDocumentProcessingException;
import com.oussama.sovereignty.application.common.UseCase;
import com.oussama.sovereignty.application.aop.TimedStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import com.oussama.sovereignty.application.common.ListUtils;
import static com.oussama.sovereignty.application.common.ExceptionUtils.runOrThrow;

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

    private final Semaphore embeddingSemaphore = new Semaphore(4);

    private final TokenTextSplitter textSplitter = new TokenTextSplitter(
            400, 350, 10, 10000, true, List.of('.', ',', '?', '!', '\n')
    );

    @Override
    @TimedStep("rag.embed.duration")
    public void indexDocument(Document document) {
        String traceId = traceContextPort.getCurrentTraceId();
        try {
            log.info("Vectorization of file : {}", document.fileName());

            // mark as processing
            manageDocumentStatusUseCase.updateDocumentStatus(document, DocumentStatus.PROCESSING, traceId, null);

            // 1. Load file content
            byte[] content = runOrThrow(
                    () -> storagePort.load(document.fileName()),
                    ex -> new FatalDocumentProcessingException("Failed to load document content from storage", ex)
            );

            // 2. Resolve parser and parse
            String extension = document.fileName().substring(document.fileName().lastIndexOf(".") + 1);
            DocumentParserPort parser = parsers.stream()
                    .filter(documentParserPort -> documentParserPort.supports(extension))
                    .findFirst()
                    .orElse(defaultParserAdapter);

            String rawText = runOrThrow(
                    () -> parser.parse(content),
                    ex -> new FatalDocumentProcessingException("Failed to parse document content", ex)
            );

            // 3. Split content
            var textChunks = textSplitter.split(
                    new org.springframework.ai.document.Document(
                            rawText,
                            Map.of("documentId", document.id().toString())
                    )
            );
            log.info("Document is split into {} chunks.", textChunks.size());

            List<String> chunks = textChunks.stream()
                    .map(org.springframework.ai.document.Document::getText)
                    .toList();

            // 4. Embed chunks
            List<List<String>> batches = ListUtils.partition(chunks, 10);
            log.info("Partitioned {} chunks into {} batches for parallel embedding.", chunks.size(), batches.size());

            runOrThrow(
                    () -> {
                        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                            List<Future<Void>> futures = new ArrayList<>();
                            for (List<String> batch : batches) {
                                futures.add(executor.submit(() -> {
                                    embeddingSemaphore.acquire();
                                    try {
                                        log.info("Embedding batch of size {} concurrently...", batch.size());
                                        vectorStorePort.embed(document.id(), batch);
                                        return null;
                                    } finally {
                                        embeddingSemaphore.release();
                                    }
                                }));
                            }

                            for (Future<Void> future : futures) {
                                try {
                                    future.get();
                                } catch (ExecutionException e) {
                                    Throwable cause = e.getCause();
                                    if (cause instanceof RuntimeException) {
                                        throw (RuntimeException) cause;
                                    }
                                    throw new RuntimeException(cause);
                                }
                            }
                        }
                        return null;
                    },
                    ex -> new TransientDocumentProcessingException("Vector store embedding failed", ex)
            );

            // mark as ready
            manageDocumentStatusUseCase.updateDocumentStatus(document, DocumentStatus.READY, traceId, null);

            log.info("Vectorisation is finished successfully : {}", document.fileName());
        } catch (FatalDocumentProcessingException ex) {
            log.error("Fatal error while processing document {}: {}", document.fileName(), ex.getMessage());
            String failureReason = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();
            manageDocumentStatusUseCase.updateDocumentStatus(document, DocumentStatus.FAILED, traceId, failureReason);
        } catch (TransientDocumentProcessingException ex) {
            log.warn("Transient error while processing document {}, will propagate for retry: {}", document.fileName(), ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while processing document {}, treating as transient: {}", document.fileName(), ex.getMessage(), ex);
            throw new TransientDocumentProcessingException("Unexpected error during indexing", ex);
        }
    }
}
