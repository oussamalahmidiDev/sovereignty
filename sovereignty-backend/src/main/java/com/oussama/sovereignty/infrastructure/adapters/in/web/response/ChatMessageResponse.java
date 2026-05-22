package com.oussama.sovereignty.infrastructure.adapters.in.web.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageResponse(
        UUID id,
        UUID chatId,
        String role,
        String content,
        LocalDateTime createdAt
) {
}
