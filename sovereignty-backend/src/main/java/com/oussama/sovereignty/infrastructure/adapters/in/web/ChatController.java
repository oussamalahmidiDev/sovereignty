package com.oussama.sovereignty.infrastructure.adapters.in.web;

import com.oussama.sovereignty.application.usecase.AskQuestionService;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.AnswerResponse;
import com.oussama.sovereignty.infrastructure.adapters.in.web.request.QuestionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
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
