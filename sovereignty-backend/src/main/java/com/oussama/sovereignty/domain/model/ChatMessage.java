package com.oussama.sovereignty.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessage(
        UUID id,
        UUID chatId,
        Role role,
        String content,
        LocalDateTime createdAt
) {
    public enum Role {
        USER,
        ASSISTANT
    }
}
