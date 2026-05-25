package com.oussama.sovereignty.infrastructure.config;

import org.springframework.ai.vectorstore.PersistentSimpleVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;

@Configuration
public class VectorStoreConfig {

    @Value("${sovereignty.vector-store.file-path:./storage/vector-store.json}")
    private String vectorStoreFilePath;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "sovereignty.ai.provider", havingValue = "ollama", matchIfMissing = true)
    public EmbeddingModel primaryOllamaEmbeddingModel(EmbeddingModel ollamaEmbeddingModel) {
        return ollamaEmbeddingModel;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "sovereignty.ai.provider", havingValue = "openai")
    public EmbeddingModel primaryOpenAiEmbeddingModel(EmbeddingModel openAiEmbeddingModel) {
        return openAiEmbeddingModel;
    }

    @Bean
    @ConditionalOnProperty(name = "sovereignty.vector-store.provider", havingValue = "pgvector")
    public VectorStore pgVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel).build();
    }

    @Bean(name = "simpleVectorStore")
    @ConditionalOnProperty(name = "sovereignty.vector-store.provider", havingValue = "internal", matchIfMissing = true)
    public VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return new PersistentSimpleVectorStore(embeddingModel, new File(vectorStoreFilePath));
    }
}
