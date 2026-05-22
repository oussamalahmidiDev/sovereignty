package com.oussama.sovereignty.infrastructure.adapters.out.persistence.repositories;

import com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatJpaRepository extends JpaRepository<ChatEntity, UUID> {
    List<ChatEntity> findAllByOrderByUpdatedAtDesc();
}
