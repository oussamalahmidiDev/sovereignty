package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.common.DomainTransactional;
import com.oussama.sovereignty.application.common.UseCase;
import com.oussama.sovereignty.application.ports.in.ManageChatUseCase;
import com.oussama.sovereignty.application.ports.out.ChatRepositoryPort;
import com.oussama.sovereignty.domain.model.Chat;
import com.oussama.sovereignty.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class ManageChatService implements ManageChatUseCase {

    private final ChatRepositoryPort chatRepositoryPort;

    @Override
    public List<Chat> findAllChats() {
        return chatRepositoryPort.findAllChats();
    }

    @Override
    public List<ChatMessage> findMessages(UUID chatId) {
        return chatRepositoryPort.findMessagesByChatId(chatId);
    }

    @Override
    @DomainTransactional
    public void deleteChat(UUID chatId) {
        chatRepositoryPort.deleteChat(chatId);
    }
}
