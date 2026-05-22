package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.ports.in.AskQuestionUseCase;
import com.oussama.sovereignty.application.ports.out.AiAgentPort;
import com.oussama.sovereignty.application.ports.out.ChatRepositoryPort;
import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import com.oussama.sovereignty.application.common.UseCase;
import com.oussama.sovereignty.application.common.StreamCallback;
import com.oussama.sovereignty.domain.model.Chat;
import com.oussama.sovereignty.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@UseCase
@RequiredArgsConstructor
public class AskQuestionService implements AskQuestionUseCase {

    private final AiAgentPort aiAgentPort;
    private final VectorStorePort vectorStorePort;
    private final ChatRepositoryPort chatRepositoryPort;

    @Override
    public Answer ask(UUID chatId, String question) {
        Chat chat = resolveChat(chatId, question);
        saveMessage(chat.id(), ChatMessage.Role.USER, question);
        List<String> relevantContext = vectorStorePort.findTopSimilar(question, 3);
        String response = aiAgentPort.askQuestion(question, relevantContext);

        saveMessage(chat.id(), ChatMessage.Role.ASSISTANT, response);
        return new Answer(chat.id(), response);
    }

    @Override
    public UUID streamAnswer(UUID chatId, String question, StreamCallback callback) {
        Chat chat = resolveChat(chatId, question);
        callback.onStart(chat.id());
        saveMessage(chat.id(), ChatMessage.Role.USER, question);

        List<String> relevantContext = vectorStorePort.findTopSimilar(question, 3);
        StringBuilder assistantAnswer = new StringBuilder();

        aiAgentPort.streamAnswer(question, relevantContext, new StreamCallback() {
            @Override
            public void onNext(String token) {
                assistantAnswer.append(token);
                callback.onNext(token);
            }

            @Override
            public void onComplete() {
                saveMessage(chat.id(), ChatMessage.Role.ASSISTANT, assistantAnswer.toString());
                callback.onComplete();
            }

            @Override
            public void onError(Throwable throwable) {
                callback.onError(throwable);
            }
        });

        return chat.id();
    }

    private Chat resolveChat(UUID chatId, String question) {
        if (chatId != null) {
            return chatRepositoryPort.findChatById(chatId)
                    .orElseGet(() -> createChat(question));
        }

        return createChat(question);
    }

    private Chat createChat(String question) {
        LocalDateTime now = LocalDateTime.now();

        return chatRepositoryPort.saveChat(new Chat(
                UUID.randomUUID(),
                buildChatName(question),
                now,
                now
        ));
    }

    private void saveMessage(UUID chatId, ChatMessage.Role role, String content) {
        LocalDateTime now = LocalDateTime.now();

        chatRepositoryPort.saveMessage(new ChatMessage(
                UUID.randomUUID(),
                chatId,
                role,
                content,
                now
        ));

        chatRepositoryPort.findChatById(chatId)
                .ifPresent(chat -> chatRepositoryPort.saveChat(new Chat(
                        chat.id(),
                        chat.name(),
                        chat.createdAt(),
                        now
                )));
    }

    private String buildChatName(String question) {
        String trimmedQuestion = question == null ? "New chat" : question.trim();
        if (trimmedQuestion.isBlank()) {
            return "New chat";
        }

        return trimmedQuestion.length() > 48
                ? trimmedQuestion.substring(0, 45) + "..."
                : trimmedQuestion;
    }
}
