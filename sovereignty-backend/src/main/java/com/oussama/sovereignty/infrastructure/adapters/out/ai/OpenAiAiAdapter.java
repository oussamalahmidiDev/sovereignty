package com.oussama.sovereignty.infrastructure.adapters.out.ai;

import com.oussama.sovereignty.application.ports.out.AiAgentPort;
import com.oussama.sovereignty.application.aop.TimedStep;
import com.oussama.sovereignty.application.common.StreamCallback;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "sovereignty.ai.provider", havingValue = "openai")
public class OpenAiAiAdapter implements AiAgentPort {

    private final OpenAiChatModel chatModel;

    @Override
    @TimedStep("rag.ask.duration")
    public String askQuestion(String userPrompt, List<String> context) {

        String systemPrompt = """
                You are Sovereignty-AI assistant. Use only info provided in : {context}
                If the answer is not found in context, abort the prompt.
                """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPrompt);
        var systemMessage = systemPromptTemplate.createMessage(Map.of(
                "context", String.join("\n", context)
        ));
        var userMessage = new UserMessage(userPrompt);

        return chatModel.call(new Prompt(List.of(systemMessage, userMessage)))
                .getResult()
                .getOutput()
                .getText();
    }

    @Override
    @TimedStep("rag.ask.stream.duration")
    public void streamAnswer(String userPrompt, List<String> context, StreamCallback callback) {

        String systemPrompt = """
                You are Sovereignty-AI assistant. Use only info provided in : {context}
                If the answer is not found in context, abort the prompt.
                """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPrompt);
        var systemMessage = systemPromptTemplate.createMessage(Map.of(
                "context", String.join("\n", context)
        ));
        var userMessage = new UserMessage(userPrompt);

        chatModel.stream(new Prompt(List.of(systemMessage, userMessage)))
                .map(response -> {
                    if (response.getResult() != null && response.getResult().getOutput() != null) {
                        String text = response.getResult().getOutput().getText();
                        return text != null ? text : "";
                    }
                    return "";
                })
                .subscribe(
                        callback::onNext,
                        callback::onError,
                        callback::onComplete
                );
    }
}
