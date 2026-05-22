package com.oussama.sovereignty.infrastructure.adapters.in.web;

import com.oussama.sovereignty.application.ports.in.AskQuestionUseCase;
import com.oussama.sovereignty.application.common.StreamCallback;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.AnswerResponse;
import com.oussama.sovereignty.infrastructure.adapters.in.web.request.QuestionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AskQuestionUseCase askQuestionUseCase;

    @PostMapping("/ask")
    public AnswerResponse ask(@RequestBody QuestionRequest request) {
        String response = askQuestionUseCase.ask(request.question());

        return new AnswerResponse(response);
    }

    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AnswerResponse> askStream(@RequestBody QuestionRequest request) {
        return Flux.create(sink -> {
            askQuestionUseCase.streamAnswer(request.question(), new StreamCallback() {
                @Override
                public void onNext(String token) {
                    sink.next(new AnswerResponse(token));
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

