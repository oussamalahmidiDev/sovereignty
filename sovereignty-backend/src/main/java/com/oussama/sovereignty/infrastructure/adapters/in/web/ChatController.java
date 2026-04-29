package com.oussama.sovereignty.infrastructure.adapters.in.web;

import com.oussama.sovereignty.application.usecase.AskQuestionService;
import com.oussama.sovereignty.infrastructure.adapters.in.web.request.AnswerResponse;
import com.oussama.sovereignty.infrastructure.adapters.in.web.request.QuestionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AskQuestionService askQuestionService;

    @PostMapping("/ask")
    public AnswerResponse ask(@RequestBody QuestionRequest request) {
        String response = askQuestionService.ask(request.question());

        return new AnswerResponse(response);
    }
}
