package com.oussama.sovereignty.infrastructure.adapters.in.web.request;

import java.util.UUID;

public record QuestionRequest(String question, UUID chatId) {
}
