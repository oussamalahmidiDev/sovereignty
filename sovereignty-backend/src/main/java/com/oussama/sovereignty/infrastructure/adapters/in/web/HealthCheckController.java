package com.oussama.sovereignty.infrastructure.adapters.in.web;

import com.oussama.sovereignty.application.usecase.HealthCheckService;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.HealthStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    @GetMapping("/status")
    public HealthStatusResponse getStatus() {
        return healthCheckService.checkHealth();
    }
}