package com.oussama.sovereignty.infrastructure.adapters.in.web;

import com.oussama.sovereignty.infrastructure.adapters.in.web.response.HealthStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Endpoints for checking backend and dependencies status")
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    @GetMapping("/status")
    @Operation(summary = "Check service health", description = "Checks the status of the local LLM engine, pgvector database, and Kafka broker.")
    public HealthStatusResponse getStatus() {
        return healthCheckService.checkHealth();
    }
}
