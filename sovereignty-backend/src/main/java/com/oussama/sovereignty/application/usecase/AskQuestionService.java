package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.ports.in.AskQuestionUseCase;
import com.oussama.sovereignty.application.ports.out.AiAgentPort;
import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AskQuestionService implements AskQuestionUseCase {

    private final AiAgentPort aiAgentPort;
    private final VectorStorePort vectorStorePort;

    @Override
    public String ask(String question) {
        List<String> relevantContext = vectorStorePort.findTopSimilar(question, 5);

        return aiAgentPort.askQuestion(question, relevantContext);
    }
}
