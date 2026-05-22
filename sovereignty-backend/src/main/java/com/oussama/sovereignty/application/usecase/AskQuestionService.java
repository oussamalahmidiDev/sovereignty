package com.oussama.sovereignty.application.usecase;

import com.oussama.sovereignty.application.ports.in.AskQuestionUseCase;
import com.oussama.sovereignty.application.ports.out.AiAgentPort;
import com.oussama.sovereignty.application.ports.out.VectorStorePort;
import com.oussama.sovereignty.application.common.UseCase;
import com.oussama.sovereignty.application.common.StreamCallback;
import lombok.RequiredArgsConstructor;

import java.util.List;


@UseCase
@RequiredArgsConstructor
public class AskQuestionService implements AskQuestionUseCase {

    private final AiAgentPort aiAgentPort;
    private final VectorStorePort vectorStorePort;

    @Override
    public String ask(String question) {
        List<String> relevantContext = vectorStorePort.findTopSimilar(question, 3);

        return aiAgentPort.askQuestion(question, relevantContext);
    }

    @Override
    public void streamAnswer(String question, StreamCallback callback) {
        List<String> relevantContext = vectorStorePort.findTopSimilar(question, 3);

        aiAgentPort.streamAnswer(question, relevantContext, callback);
    }
}

