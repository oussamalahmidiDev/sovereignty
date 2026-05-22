package com.oussama.sovereignty.infrastructure.adapters.out.persistence.mappers;

import com.oussama.sovereignty.domain.model.Document;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities.DocumentEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DocumentEntityMapper {

    public static DocumentEntity toEntity(Document document) {
        return DocumentEntity.builder()
                .id(document.id())
                .fileName(document.fileName())
                .contentType(document.contentType())
                .status(document.status())
                .createdAt(document.createdAt())
                .build();
    }

    public static Document toDomain(DocumentEntity entity) {
        return new Document(
                entity.getId(),
                entity.getFileName(),
                entity.getContentType(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
