package com.oussama.sovereignty.infrastructure.adapters.in.web.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatResponse(
        UUID id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
