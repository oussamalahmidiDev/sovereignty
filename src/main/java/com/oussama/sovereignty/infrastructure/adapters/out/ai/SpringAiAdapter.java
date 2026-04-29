package com.oussama.sovereignty.infrastructure.adapters.out.ai;

import com.oussama.sovereignty.application.ports.out.AiAgentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SpringAiAdapter implements AiAgentPort {

    private final EmbeddingModel localEmbeddingModel;
    private final OllamaChatModel chatModel;

    @Override
    public String askQuestion(String userPrompt, List<String> context) {

        String systemPrompt = """
                You are Sovereignty-AI assistant. Use only info provided in : {context}
                If the answer is not found in context, abort the prompt.
                """;

        SystemPromptTemplate promptTemplate = new SystemPromptTemplate(systemPrompt);
        var message = promptTemplate.createMessage(Map.of(
                "context", String.join("\n", context)
        ));
        var userMessage = new UserMessage(userPrompt);

        return chatModel.call(new Prompt(List.of(message, userMessage)))
                .getResult()
                .getOutput()
                .getText();
    }

    @Override
    public float[] embed(String text) {
        return localEmbeddingModel.embed(text);
    }
}
