package com.oussama.sovereignty.infrastructure.config;

import com.oussama.sovereignty.application.ports.out.*;
import com.oussama.sovereignty.application.usecase.AskQuestionService;
import com.oussama.sovereignty.application.usecase.DocumentStatusService;
import com.oussama.sovereignty.application.usecase.ManageDocumentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableTransactionManagement
public class ApplicationServiceConfig {

    @Bean
    public AskQuestionService askQuestionService(AiAgentPort aiAgentPort, VectorStorePort vectorStorePort) {
        return new AskQuestionService(aiAgentPort, vectorStorePort);
    }

    @Bean
    public DocumentStatusService documentStatusService(
            DocumentRepositoryPort documentRepositoryPort,
            DocumentStatusNotificationPort documentStatusNotificationPort,
            DocumentEventRepositoryPort documentEventRepositoryPort) {
        return new DocumentStatusService(documentRepositoryPort, documentStatusNotificationPort, documentEventRepositoryPort);
    }

    @Bean
    @Transactional
    public ManageDocumentService manageDocumentService(
            DocumentRepositoryPort documentRepository,
            DocumentStoragePort storagePort,
            DocumentEventPublisherPort eventPublisher,
            VectorStorePort vectorStorePort,
            DocumentEventRepositoryPort documentEventRepository,
            TraceContextPort traceContextPort) {
        return new ManageDocumentService(
                documentRepository,
                storagePort,
                eventPublisher,
                vectorStorePort,
                documentEventRepository,
                traceContextPort
        );
    }
}
