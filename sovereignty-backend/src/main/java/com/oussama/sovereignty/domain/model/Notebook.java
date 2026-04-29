package com.oussama.sovereignty.domain.model;

import java.util.List;
import java.util.UUID;

public record Notebook(
        UUID id,
        String name,
        List<Document> documents
) {
}
