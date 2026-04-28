package com.oussama.sovereignty.infrastructure.adapters.out.persistence;

import com.oussama.sovereignty.application.ports.out.DocumentRepositoryPort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities.DocumentEntity;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.repositories.DocumentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentPersistenceAdapter implements DocumentRepositoryPort {

    private final DocumentJpaRepository repository;

    @Override
    public void save(Document document) {

        DocumentEntity entity = DocumentEntity.builder()
                .id(document.id())
                .fileName(document.fileName())
                .contentType(document.contentType())
                .status(document.status())
                .createdAt(document.createdAt())
                .build();

        repository.save(entity);
    }
}
