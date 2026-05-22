package com.oussama.sovereignty.infrastructure.adapters.in.web;

import com.oussama.sovereignty.application.common.StreamCallback;
import com.oussama.sovereignty.application.ports.in.AskQuestionUseCase;
import com.oussama.sovereignty.application.ports.in.ManageChatUseCase;
import com.oussama.sovereignty.domain.model.Chat;
import com.oussama.sovereignty.domain.model.ChatMessage;
import com.oussama.sovereignty.infrastructure.adapters.in.web.request.QuestionRequest;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.AnswerResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatControllerTest {

    private final FakeAskQuestionUseCase askQuestionUseCase = new FakeAskQuestionUseCase();
    private final ChatController chatController = new ChatController(askQuestionUseCase, new NoopManageChatUseCase());

    @Test
    void ask_shouldReturnAnswerResponse() {
        UUID chatId = UUID.randomUUID();
        QuestionRequest request = new QuestionRequest("Hello", chatId);
        askQuestionUseCase.answer = new AskQuestionUseCase.Answer(chatId, "World");

        AnswerResponse response = chatController.ask(request);

        assertEquals("World", response.response());
        assertEquals(chatId, response.chatId());
        assertEquals(chatId, askQuestionUseCase.chatId);
        assertEquals("Hello", askQuestionUseCase.question);
    }

    @Test
    void askStream_shouldReturnFluxOfAnswerResponse() {
        UUID chatId = UUID.randomUUID();
        QuestionRequest request = new QuestionRequest("Hello", null);
        askQuestionUseCase.streamChatId = chatId;
        askQuestionUseCase.streamTokens = List.of("Wor", "ld");

        Flux<AnswerResponse> responseFlux = chatController.askStream(request);

        List<AnswerResponse> results = responseFlux.collectList().block();

        assertEquals(3, results.size());
        assertEquals("", results.get(0).response());
        assertEquals(chatId, results.get(0).chatId());
        assertEquals("Wor", results.get(1).response());
        assertEquals(chatId, results.get(1).chatId());
        assertEquals("ld", results.get(2).response());
        assertEquals(chatId, results.get(2).chatId());
        assertEquals("Hello", askQuestionUseCase.question);
    }

    private static class FakeAskQuestionUseCase implements AskQuestionUseCase {
        private UUID chatId;
        private String question;
        private Answer answer;
        private UUID streamChatId;
        private List<String> streamTokens = List.of();

        @Override
        public Answer ask(UUID chatId, String question) {
            this.chatId = chatId;
            this.question = question;
            return answer;
        }

        @Override
        public UUID streamAnswer(UUID chatId, String question, StreamCallback callback) {
            this.chatId = chatId;
            this.question = question;
            callback.onStart(streamChatId);
            streamTokens.forEach(callback::onNext);
            callback.onComplete();
            return streamChatId;
        }
    }

    private static class NoopManageChatUseCase implements ManageChatUseCase {
        @Override
        public List<Chat> findAllChats() {
            return List.of();
        }

        @Override
        public List<ChatMessage> findMessages(UUID chatId) {
            return List.of();
        }

        @Override
        public void deleteChat(UUID chatId) {
        }
    }
}
