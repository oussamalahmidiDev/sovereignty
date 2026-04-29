package com.oussama.sovereignty.infrastructure.adapters.out.persistence;

import com.oussama.sovereignty.application.ports.out.DocumentRepositoryPort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities.DocumentEntity;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.repositories.DocumentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @Override
    public List<Document> findAllDocuments() {
        List<DocumentEntity> entities = repository.findAll();

        return entities.stream()
                .map(entity ->
                        new Document(entity.getId(), entity.getFileName(), entity.getContentType(), entity.getStatus(), null, entity.getCreatedAt())
                )
                .toList();
    }

    @Override
    public void deleteDocument(String id) {

    }
}
