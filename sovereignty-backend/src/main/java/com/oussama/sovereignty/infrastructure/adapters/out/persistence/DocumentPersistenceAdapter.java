package com.oussama.sovereignty.infrastructure.adapters.out.persistence;

import com.oussama.sovereignty.application.ports.out.DocumentRepositoryPort;
import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities.DocumentEntity;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.mappers.DocumentEntityMapper;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.repositories.DocumentJpaRepository;
import com.oussama.sovereignty.application.aop.TimedStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DocumentPersistenceAdapter implements DocumentRepositoryPort {

    private final DocumentJpaRepository repository;

    @Override
    @TimedStep("rag.store")
    public void save(Document document) {
        repository.save(DocumentEntityMapper.toEntity(document));
    }

    @Override
    public List<Document> findAllDocuments() {
        List<DocumentEntity> entities = repository.findAllByOrderByCreatedAtDesc();

        return entities.stream()
                .map(DocumentEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Document> findDocumentById(UUID id) {
        return repository.findById(id).map(DocumentEntityMapper::toDomain);
    }

    @Override
    public void deleteDocument(UUID id) {
        repository.deleteById(id);
    }
}
