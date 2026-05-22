package com.oussama.sovereignty.infrastructure.observability;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin - Profiling", description = "Endpoints for Java Flight Recorder (JFR) profiling controls")
public class JfrRestController {

    private final JfrService jfrService;

    @PostMapping("/start")
    @Operation(summary = "Start JFR recording", description = "Starts a new Java Flight Recorder profiling session for the specified duration (in seconds).")
    public String start(@RequestParam(defaultValue = "60") long duration) throws Exception {
        return jfrService.startRecording(duration);
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop JFR recording", description = "Forces the active Java Flight Recorder profiling session to stop.")
    public String stop() {
        return jfrService.stopRecording();
    }

    @GetMapping("/download")
    @Operation(summary = "Download JFR recording file", description = "Downloads the generated Java Flight Recorder (.jfr) binary file for analysis.")
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
