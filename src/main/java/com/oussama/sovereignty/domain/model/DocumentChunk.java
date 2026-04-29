package com.oussama.sovereignty.domain.model;

import java.util.UUID;

public record DocumentChunk(
        UUID id,
        UUID documentId,
        String content,
        float[] vector
) {
}
