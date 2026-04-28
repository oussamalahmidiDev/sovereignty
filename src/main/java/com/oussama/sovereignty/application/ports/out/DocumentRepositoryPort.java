package com.oussama.sovereignty.application.ports.out;

import com.oussama.sovereignty.domain.model.Document;

public interface DocumentRepositoryPort {
    void save(Document document);
}
