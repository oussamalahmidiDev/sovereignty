package com.oussama.sovereignty.application.ports.in;

import com.oussama.sovereignty.domain.model.Chat;
import com.oussama.sovereignty.domain.model.ChatMessage;

import java.util.List;
import java.util.UUID;

public interface ManageChatUseCase {
    List<Chat> findAllChats();
    List<ChatMessage> findMessages(UUID chatId);
    void deleteChat(UUID chatId);
}
