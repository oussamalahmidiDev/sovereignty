package com.oussama.sovereignty.infrastructure.adapters.in.web.request;

import java.util.UUID;

public record DocumentDeletionRequest(UUID id, String fileName) {
}
