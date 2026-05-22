package com.oussama.sovereignty.infrastructure.adapters.out.persistence.mappers;

import com.oussama.sovereignty.domain.model.Chat;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities.ChatEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatEntityMapper {

    public static ChatEntity toEntity(Chat chat) {
        return ChatEntity.builder()
                .id(chat.id())
                .name(chat.name())
                .createdAt(chat.createdAt())
                .updatedAt(chat.updatedAt())
                .build();
    }

    public static Chat toDomain(ChatEntity entity) {
        return new Chat(
                entity.getId(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
