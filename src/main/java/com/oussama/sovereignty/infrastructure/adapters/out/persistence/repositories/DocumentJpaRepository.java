package com.oussama.sovereignty.infrastructure.adapters.out.persistence.repositories;

import com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentJpaRepository extends JpaRepository<DocumentEntity, UUID> {
}
