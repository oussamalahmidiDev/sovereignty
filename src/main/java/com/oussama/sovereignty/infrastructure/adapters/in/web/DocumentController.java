package com.oussama.sovereignty.infrastructure.adapters.in.web;

import com.oussama.sovereignty.application.usecase.ImportDocumentService;
import com.oussama.sovereignty.infrastructure.adapters.in.web.request.AcceptedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor @Slf4j
public class DocumentController {

    private final ImportDocumentService importDocumentService;

    @PostMapping("/upload")
    public ResponseEntity<AcceptedResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        log.info("Received a new file : {} ({})", file.getOriginalFilename(), file.getContentType());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new AcceptedResponse("File should not be empty.", file.getOriginalFilename()));
        }

        try {
            importDocumentService.importDocument(
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
}
