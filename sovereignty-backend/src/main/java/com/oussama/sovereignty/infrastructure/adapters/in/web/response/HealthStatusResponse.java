package com.oussama.sovereignty.infrastructure.adapters.in.web.response;

public record HealthStatusResponse(
        HealthStatus status,
        String message,
        HealthDetails details
) {
    public record HealthDetails(
            boolean ollama,
            boolean database,
            boolean kafka
    ) {}

    public enum HealthStatus {
        UP, DOWN
    }
}