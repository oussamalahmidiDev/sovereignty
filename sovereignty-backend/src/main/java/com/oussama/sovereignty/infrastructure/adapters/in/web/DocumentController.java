package com.oussama.sovereignty.infrastructure.adapters.in.web;

import com.oussama.sovereignty.application.ports.out.DocumentEventRepositoryPort;
import com.oussama.sovereignty.application.ports.out.DocumentEventRepositoryPort.EventDetails;
import com.oussama.sovereignty.application.ports.in.ManageDocumentUseCase.DownloadedDocument;
import com.oussama.sovereignty.application.usecase.ManageDocumentService;
import com.oussama.sovereignty.infrastructure.adapters.in.web.request.DocumentDeletionRequest;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.AcceptedResponse;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.DocumentResponse;
import com.oussama.sovereignty.infrastructure.adapters.out.sse.DocumentStatusNotificationAdapter;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.oussama.sovereignty.domain.model.Document;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor @Slf4j
public class DocumentController {

    private final ManageDocumentService manageDocumentService;
    private final DocumentStatusNotificationAdapter documentStatusNotificationAdapter;
    private final DocumentEventRepositoryPort documentEventRepositoryPort;

    @PostMapping("/upload")
    public ResponseEntity<AcceptedResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        log.info("Received a new file : {} ({})", file.getOriginalFilename(), file.getContentType());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new AcceptedResponse("File should not be empty.", file.getOriginalFilename()));
        }

        try {
            manageDocumentService.importDocument(
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
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        List<Document> documents = manageDocumentService.findAllDocuments();
        List<UUID> documentIds = documents.stream().map(Document::id).toList();
        
        Map<UUID, EventDetails> failureDetails = 
                documentEventRepositoryPort.findFailureDetailsByDocumentIds(documentIds);

        return ResponseEntity.ok(documents.stream()
                .map(document -> {
                    var details = failureDetails.get(document.id());
                    String traceId = details != null ? details.traceId() : null;
                    String failureReason = details != null ? details.failureReason() : null;
                    return new DocumentResponse(
                            document.id(),
                            document.fileName(),
                            document.contentType(),
                            document.status().name(),
                            document.createdAt(),
                            traceId,
                            failureReason
                    );
                })
                .toList());
    }

    @GetMapping("/{documentId}/subscribe")
    public SseEmitter subscribeToDocumentStatus(@PathVariable String documentId) {
        return documentStatusNotificationAdapter.subscribe(documentId);
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable UUID documentId) {
        try {
            DownloadedDocument document = manageDocumentService.downloadDocument(documentId);
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
    public void deleteDocument(@RequestBody DocumentDeletionRequest request) {
        manageDocumentService.deleteDocument(request.id(), request.fileName());
    }
}
