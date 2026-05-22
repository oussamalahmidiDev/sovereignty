package com.oussama.sovereignty.infrastructure.adapters.out.persistence.entities;

import com.oussama.sovereignty.domain.model.ChatMessage.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageEntity {

    @Id
    private UUID id;

    private UUID chatId;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String content;
    private LocalDateTime createdAt;
}
