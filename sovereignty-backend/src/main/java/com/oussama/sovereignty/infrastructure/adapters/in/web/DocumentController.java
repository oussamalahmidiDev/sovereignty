package com.oussama.sovereignty.infrastructure.adapters.in.web;

import com.oussama.sovereignty.application.usecase.DocumentStatusService;
import com.oussama.sovereignty.application.usecase.ManageDocumentService;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.infrastructure.adapters.in.web.request.DocumentDeletionRequest;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.AcceptedResponse;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.DocumentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/documents")
@RequiredArgsConstructor @Slf4j
public class DocumentController {

    private final ManageDocumentService manageDocumentService;
    private final DocumentStatusService documentStatusService;

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
        return ResponseEntity.ok(manageDocumentService.findAllDocuments()
                .stream()
                .map(document -> new DocumentResponse(
                        document.id(),
                        document.fileName(),
                        document.contentType(),
                        document.status().name(),
                        document.createdAt()
                ))
                .toList());
    }

    @GetMapping("/{documentId}/subscribe")
    public SseEmitter subscribeToDocumentStatus(@PathVariable String documentId) {
        return documentStatusService.subscribe(documentId);
    }

    @DeleteMapping
    public void deleteDocument(@RequestBody DocumentDeletionRequest request) {
        manageDocumentService.deleteDocument(request.id(), request.fileName());
    }
}
