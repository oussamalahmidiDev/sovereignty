package com.oussama.sovereignty.infrastructure.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "sovereignty.ai.provider", havingValue = "ollama", matchIfMissing = true)
    public ChatModel primaryOllamaChatModel(ChatModel ollamaChatModel) {
        return ollamaChatModel;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "sovereignty.ai.provider", havingValue = "openai")
    public ChatModel primaryOpenAiChatModel(ChatModel openAiChatModel) {
        return openAiChatModel;
    }
}
