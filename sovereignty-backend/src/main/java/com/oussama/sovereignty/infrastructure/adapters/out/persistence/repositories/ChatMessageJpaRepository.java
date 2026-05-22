package com.oussama.sovereignty.infrastructure.adapters.out.persistence.repositories;

import com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageEntity, UUID> {
    List<ChatMessageEntity> findAllByChatIdOrderByCreatedAtAsc(UUID chatId);
    void deleteAllByChatId(UUID chatId);
}
