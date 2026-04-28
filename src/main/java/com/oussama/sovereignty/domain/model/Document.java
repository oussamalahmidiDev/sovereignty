package com.oussama.sovereignty.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Document(
        UUID id,
        String fileName,
        String contentType,
        byte[] content,
        LocalDateTime createdAt
) {
}
