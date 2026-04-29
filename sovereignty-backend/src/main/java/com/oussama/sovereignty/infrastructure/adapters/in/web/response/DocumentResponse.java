package com.oussama.sovereignty.infrastructure.adapters.in.web.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String fileName,
        String fileType,
        LocalDateTime createdAt
) {
}
