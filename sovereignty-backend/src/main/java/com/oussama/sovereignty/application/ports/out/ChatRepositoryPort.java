package com.oussama.sovereignty.application.ports.out;

import com.oussama.sovereignty.domain.model.Chat;
import com.oussama.sovereignty.domain.model.ChatMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepositoryPort {
    Chat saveChat(Chat chat);
    ChatMessage saveMessage(ChatMessage message);
    List<Chat> findAllChats();
    Optional<Chat> findChatById(UUID chatId);
    List<ChatMessage> findMessagesByChatId(UUID chatId);
    void deleteChat(UUID chatId);
}
