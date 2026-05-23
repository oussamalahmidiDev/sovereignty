package com.oussama.sovereignty.infrastructure.adapters.in.web;

import com.oussama.sovereignty.application.ports.in.ManageDocumentUseCase;
import com.oussama.sovereignty.application.ports.in.ManageDocumentUseCase.DownloadedDocument;
import com.oussama.sovereignty.application.ports.in.ManageDocumentUseCase.DocumentDetails;
import com.oussama.sovereignty.application.common.DocumentStatusCallback;
import com.oussama.sovereignty.application.common.Subscription;
import com.oussama.sovereignty.infrastructure.adapters.in.web.request.DocumentDeletionRequest;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.AcceptedResponse;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.DocumentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor @Slf4j
@Tag(name = "Documents", description = "Endpoints for uploading, listing, downloading, and deleting indexable documents")
public class DocumentController {

    private final ManageDocumentUseCase manageDocumentUseCase;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and index a document", description = "Uploads a text or markdown file, extracts its chunks, generates embeddings, and saves them to the pgvector database.")
    public ResponseEntity<AcceptedResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        log.info("Received a new file : {} ({})", file.getOriginalFilename(), file.getContentType());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new AcceptedResponse("File should not be empty.", file.getOriginalFilename()));
        }

        try {
            manageDocumentUseCase.importDocument(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new AcceptedResponse("File uploaded successfully.", file.getOriginalFilename()));

        } catch (IOException e) {
            log.error("Error while handling the file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AcceptedResponse("Error while handling the file.", file.getOriginalFilename()));
        }
    }

    @GetMapping
    @Operation(summary = "List all documents", description = "Retrieves a list of all uploaded documents along with their processing status and any failure details.")
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        List<DocumentDetails> documents = manageDocumentUseCase.findAllDocuments();

        return ResponseEntity.ok(documents.stream()
                .map(document -> new DocumentResponse(
                        document.id(),
                        document.fileName(),
                        document.contentType(),
                        document.status(),
                        document.createdAt(),
                        document.traceId(),
                        document.failureReason()
                ))
                .toList());
    }

    @GetMapping(value = "/{documentId}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to document status updates", description = "Establishes a Server-Sent Events (SSE) connection to receive real-time updates on a document's processing status.")
    public Flux<ServerSentEvent<String>> subscribeToDocumentStatus(@PathVariable String documentId) {
        UUID docId = UUID.fromString(documentId);
        return Flux.create(sink -> {
            Subscription subscription = manageDocumentUseCase.subscribeToStatus(docId, new DocumentStatusCallback() {
                @Override
                public void onStatusChanged(String status) {
                    sink.next(ServerSentEvent.<String>builder()
                            .id(UUID.randomUUID().toString())
                            .event("status-update")
                            .data(status)
                            .build());
                }

                @Override
                public void onComplete() {
                    sink.complete();
                }

                @Override
                public void onError(Throwable throwable) {
                    sink.error(throwable);
                }
            });
            sink.onDispose(subscription::unsubscribe);
        });
    }

    @GetMapping("/{documentId}/download")
    @Operation(summary = "Download raw document", description = "Downloads the original raw text/markdown file content for a given document ID.")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable UUID documentId) {
        try {
            DownloadedDocument document = manageDocumentUseCase.downloadDocument(documentId);
            MediaType contentType = document.contentType() != null
                    ? MediaType.parseMediaType(document.contentType())
                    : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                             .filename(document.fileName())
                            .build()
                            .toString())
                    .body(document.content());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found", e);
        }
    }

    @DeleteMapping
    @Operation(summary = "Delete a document", description = "Removes a document from the system, deleting both its raw content, metadata, and all vector store embeddings.")
    public void deleteDocument(@RequestBody DocumentDeletionRequest request) {
        manageDocumentUseCase.deleteDocument(request.id(), request.fileName());
    }
}
