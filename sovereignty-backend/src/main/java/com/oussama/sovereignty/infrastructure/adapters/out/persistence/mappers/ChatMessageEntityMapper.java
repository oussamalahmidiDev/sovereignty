package com.oussama.sovereignty.infrastructure.adapters.out.persistence.mappers;

import com.oussama.sovereignty.domain.model.ChatMessage;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities.ChatMessageEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatMessageEntityMapper {

    public static ChatMessageEntity toEntity(ChatMessage message) {
        return ChatMessageEntity.builder()
                .id(message.id())
                .chatId(message.chatId())
                .role(message.role())
                .content(message.content())
                .createdAt(message.createdAt())
                .build();
    }

    public static ChatMessage toDomain(ChatMessageEntity entity) {
        return new ChatMessage(
                entity.getId(),
                entity.getChatId(),
                entity.getRole(),
                entity.getContent(),
                entity.getCreatedAt()
        );
    }
}
