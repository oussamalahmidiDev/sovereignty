package com.oussama.sovereignty.infrastructure.observability;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

@RestController
@RequestMapping("/admin/jfr")
@RequiredArgsConstructor
public class JfrRestController {

    private final JfrService jfrService;

    @PostMapping("/start")
    public String start(@RequestParam(defaultValue = "60") long duration) throws Exception {
        return jfrService.startRecording(duration);
    }

    @PostMapping("/stop")
    public String stop() {
        return jfrService.stopRecording();
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download() {

        Path file = jfrService.getRecordingFile();

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=recording.jfr")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
