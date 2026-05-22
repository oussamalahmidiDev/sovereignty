package com.oussama.sovereignty.infrastructure.adapters.out.persistence;

import com.oussama.sovereignty.application.ports.out.ChatRepositoryPort;
import com.oussama.sovereignty.domain.model.Chat;
import com.oussama.sovereignty.domain.model.ChatMessage;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.mappers.ChatEntityMapper;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.mappers.ChatMessageEntityMapper;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.repositories.ChatJpaRepository;
import com.oussama.sovereignty.infrastructure.adapters.out.persistence.repositories.ChatMessageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatPersistenceAdapter implements ChatRepositoryPort {

    private final ChatJpaRepository chatRepository;
    private final ChatMessageJpaRepository chatMessageRepository;

    @Override
    public Chat saveChat(Chat chat) {
        return ChatEntityMapper.toDomain(chatRepository.save(ChatEntityMapper.toEntity(chat)));
    }

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        return ChatMessageEntityMapper.toDomain(chatMessageRepository.save(ChatMessageEntityMapper.toEntity(message)));
    }

    @Override
    public List<Chat> findAllChats() {
        return chatRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(ChatEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Chat> findChatById(UUID chatId) {
        return chatRepository.findById(chatId).map(ChatEntityMapper::toDomain);
    }

    @Override
    public List<ChatMessage> findMessagesByChatId(UUID chatId) {
        return chatMessageRepository.findAllByChatIdOrderByCreatedAtAsc(chatId).stream()
                .map(ChatMessageEntityMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteChat(UUID chatId) {
        chatMessageRepository.deleteAllByChatId(chatId);
        chatRepository.deleteById(chatId);
    }
}
