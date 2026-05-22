package com.oussama.sovereignty.infrastructure.adapters.in.web;

import com.oussama.sovereignty.application.ports.in.AskQuestionUseCase;
import com.oussama.sovereignty.application.ports.in.AskQuestionUseCase.Answer;
import com.oussama.sovereignty.application.ports.in.ManageChatUseCase;
import com.oussama.sovereignty.application.common.StreamCallback;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.AnswerResponse;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.ChatMessageResponse;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.ChatResponse;
import com.oussama.sovereignty.infrastructure.adapters.in.web.request.QuestionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AskQuestionUseCase askQuestionUseCase;
    private final ManageChatUseCase manageChatUseCase;

    @GetMapping
    public List<ChatResponse> findAllChats() {
        return manageChatUseCase.findAllChats().stream()
                .map(chat -> new ChatResponse(chat.id(), chat.name(), chat.createdAt(), chat.updatedAt()))
                .toList();
    }

    @GetMapping("/{chatId}/messages")
    public List<ChatMessageResponse> findMessages(@PathVariable UUID chatId) {
        return manageChatUseCase.findMessages(chatId).stream()
                .map(message -> new ChatMessageResponse(
                        message.id(),
                        message.chatId(),
                        message.role().name(),
                        message.content(),
                        message.createdAt()
                ))
                .toList();
    }

    @DeleteMapping("/{chatId}")
    public void deleteChat(@PathVariable UUID chatId) {
        manageChatUseCase.deleteChat(chatId);
    }

    @PostMapping("/ask")
    public AnswerResponse ask(@RequestBody QuestionRequest request) {
        Answer answer = askQuestionUseCase.ask(request.chatId(), request.question());

        return new AnswerResponse(answer.response(), answer.chatId());
    }

    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AnswerResponse> askStream(@RequestBody QuestionRequest request) {
        return Flux.create(sink -> {
            AtomicReference<UUID> chatId = new AtomicReference<>(request.chatId());
            askQuestionUseCase.streamAnswer(request.chatId(), request.question(), new StreamCallback() {
                @Override
                public void onStart(UUID resolvedChatId) {
                    chatId.set(resolvedChatId);
                    sink.next(new AnswerResponse("", resolvedChatId));
                }

                @Override
                public void onNext(String token) {
                    sink.next(new AnswerResponse(token, chatId.get()));
                }

                @Override
                public void onComplete() {
                    sink.complete();
                }

                @Override
                public void onError(Throwable throwable) {
                    sink.error(throwable);
                }
            });
        });
    }
}
