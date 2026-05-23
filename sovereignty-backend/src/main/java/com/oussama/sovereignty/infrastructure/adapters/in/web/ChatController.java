package com.oussama.sovereignty.infrastructure.adapters.in.web;

import com.oussama.sovereignty.application.ports.in.AskQuestionUseCase;
import com.oussama.sovereignty.application.ports.in.AskQuestionUseCase.Answer;
import com.oussama.sovereignty.application.ports.in.ManageChatUseCase;
import com.oussama.sovereignty.application.common.StreamCallback;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.AnswerResponse;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.ChatMessageResponse;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.ChatResponse;
import com.oussama.sovereignty.infrastructure.adapters.in.web.request.QuestionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor @Slf4j
@Tag(name = "Chat", description = "Endpoints for managing chat conversations and messages")
public class ChatController {

    private final AskQuestionUseCase askQuestionUseCase;
    private final ManageChatUseCase manageChatUseCase;

    @GetMapping
    @Operation(summary = "Retrieve all chat conversations", description = "Returns a list of all active chat sessions.")
    public List<ChatResponse> findAllChats() {
        log.info("Retrieving all chat conversations");
        return manageChatUseCase.findAllChats().stream()
                .map(chat -> new ChatResponse(chat.id(), chat.name(), chat.createdAt(), chat.updatedAt()))
                .toList();
    }

    @GetMapping("/{chatId}/messages")
    @Operation(summary = "Retrieve chat messages", description = "Fetches the full message history for a specific chat conversation.")
    public List<ChatMessageResponse> findMessages(@PathVariable UUID chatId) {
        log.info("Retrieving message history for chat session: {}", chatId);
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
    @Operation(summary = "Delete a chat conversation", description = "Deletes a specific chat conversation along with all its messages.")
    public void deleteChat(@PathVariable UUID chatId) {
        log.info("Deleting chat session: {}", chatId);
        manageChatUseCase.deleteChat(chatId);
    }

    @PostMapping("/ask")
    @Operation(summary = "Submit a blocking query", description = "Submits a question to the chat session and waits for the full assistant response.")
    public AnswerResponse ask(@RequestBody QuestionRequest request) {
        log.info("Received blocking query for chat session: {}", request.chatId());
        Answer answer = askQuestionUseCase.ask(request.chatId(), request.question());

        return new AnswerResponse(answer.response(), answer.chatId());
    }

    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Submit a streaming query", description = "Submits a question to the chat session and streams the response token-by-token using Server-Sent Events (SSE).")
    public Flux<AnswerResponse> askStream(@RequestBody QuestionRequest request) {
        log.info("Received streaming query for chat session: {}", request.chatId());
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
