package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.common.StreamCallback;
import com.oussama.sovereignty.application.ports.out.AiAgentPort;
import com.oussama.sovereignty.application.ports.out.ChatRepositoryPort;
import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import com.oussama.sovereignty.domain.model.Chat;
import com.oussama.sovereignty.domain.model.ChatMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AskQuestionServiceTest {

    private final FakeAiAgentPort aiAgentPort = new FakeAiAgentPort();
    private final FakeVectorStorePort vectorStorePort = new FakeVectorStorePort();
    private final FakeChatRepositoryPort chatRepositoryPort = new FakeChatRepositoryPort();
    private final AskQuestionService askQuestionService = new AskQuestionService(aiAgentPort, vectorStorePort, chatRepositoryPort);

    @Test
    void ask_shouldRetrieveTop3SimilarAskQuestionAndPersistMessages() {
        String question = "What is sovereignty?";
        vectorStorePort.context = List.of("context 1", "context 2", "context 3");
        aiAgentPort.response = "Response";

        AskQuestionService.Answer result = askQuestionService.ask(null, question);

        assertNotNull(result.chatId());
        assertEquals("Response", result.response());
        assertEquals(question, vectorStorePort.question);
        assertEquals(3, vectorStorePort.limit);
        assertEquals(question, aiAgentPort.question);
        assertEquals(vectorStorePort.context, aiAgentPort.context);
        assertEquals(1, chatRepositoryPort.chats.size());
        assertEquals(2, chatRepositoryPort.messages.size());
        assertEquals(ChatMessage.Role.USER, chatRepositoryPort.messages.get(0).role());
        assertEquals(question, chatRepositoryPort.messages.get(0).content());
        assertEquals(ChatMessage.Role.ASSISTANT, chatRepositoryPort.messages.get(1).role());
        assertEquals("Response", chatRepositoryPort.messages.get(1).content());
    }

    @Test
    void streamAnswer_shouldRetrieveTop3SimilarStreamQuestionAndPersistMessages() {
        String question = "What is AI?";
        CapturingStreamCallback callback = new CapturingStreamCallback();
        vectorStorePort.context = List.of("context 1", "context 2", "context 3");
        aiAgentPort.streamTokens = List.of("Hel", "lo");

        UUID chatId = askQuestionService.streamAnswer(null, question, callback);

        assertNotNull(chatId);
        assertEquals(chatId, callback.chatId);
        assertEquals(List.of("Hel", "lo"), callback.tokens);
        assertEquals(1, callback.completeCount);
        assertEquals(question, vectorStorePort.question);
        assertEquals(3, vectorStorePort.limit);
        assertEquals(question, aiAgentPort.question);
        assertEquals(vectorStorePort.context, aiAgentPort.context);
        assertEquals(2, chatRepositoryPort.messages.size());
        assertEquals(ChatMessage.Role.USER, chatRepositoryPort.messages.get(0).role());
        assertEquals(question, chatRepositoryPort.messages.get(0).content());
        assertEquals(ChatMessage.Role.ASSISTANT, chatRepositoryPort.messages.get(1).role());
        assertEquals("Hello", chatRepositoryPort.messages.get(1).content());
    }

    private static class FakeAiAgentPort implements AiAgentPort {
        private String question;
        private List<String> context;
        private String response;
        private List<String> streamTokens = List.of();

        @Override
        public String askQuestion(String question, List<String> relevantContext) {
            this.question = question;
            this.context = relevantContext;
            return response;
        }

        @Override
        public void streamAnswer(String question, List<String> relevantContext, StreamCallback callback) {
            this.question = question;
            this.context = relevantContext;
            streamTokens.forEach(callback::onNext);
            callback.onComplete();
        }
    }

    private static class FakeVectorStorePort implements VectorStorePort {
        private String question;
        private int limit;
        private List<String> context = List.of();

        @Override
        public void embed(UUID documentId, String content) {
        }

        @Override
        public void embed(UUID documentId, List<String> contents) {
        }

        @Override
        public void clean(UUID documentId) {
        }

        @Override
        public List<String> findTopSimilar(String question, int topK) {
            this.question = question;
            this.limit = topK;
            return context;
        }
    }

    private static class FakeChatRepositoryPort implements ChatRepositoryPort {
        private final List<Chat> chats = new ArrayList<>();
        private final List<ChatMessage> messages = new ArrayList<>();

        @Override
        public Chat saveChat(Chat chat) {
            chats.removeIf(existingChat -> existingChat.id().equals(chat.id()));
            chats.add(chat);
            return chat;
        }

        @Override
        public ChatMessage saveMessage(ChatMessage message) {
            messages.add(message);
            return message;
        }

        @Override
        public List<Chat> findAllChats() {
            return chats;
        }

        @Override
        public Optional<Chat> findChatById(UUID chatId) {
            return chats.stream()
                    .filter(chat -> chat.id().equals(chatId))
                    .findFirst();
        }

        @Override
        public List<ChatMessage> findMessagesByChatId(UUID chatId) {
            return messages.stream()
                    .filter(message -> message.chatId().equals(chatId))
                    .toList();
        }

        @Override
        public void deleteChat(UUID chatId) {
            chats.removeIf(chat -> chat.id().equals(chatId));
            messages.removeIf(message -> message.chatId().equals(chatId));
        }
    }

    private static class CapturingStreamCallback implements StreamCallback {
        private UUID chatId;
        private final List<String> tokens = new ArrayList<>();
        private int completeCount;

        @Override
        public void onStart(UUID chatId) {
            this.chatId = chatId;
        }

        @Override
        public void onNext(String token) {
            tokens.add(token);
        }

        @Override
        public void onComplete() {
            completeCount++;
        }

        @Override
        public void onError(Throwable throwable) {
        }
    }
}
