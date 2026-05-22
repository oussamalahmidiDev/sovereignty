package com.oussama.sovereignty.infrastructure.adapters.in.web.response;

import java.util.UUID;

public record AnswerResponse(String response, UUID chatId) {
}
