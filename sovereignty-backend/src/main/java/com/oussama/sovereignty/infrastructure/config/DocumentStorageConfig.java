package com.oussama.sovereignty.infrastructure.config;

import com.oussama.sovereignty.application.ports.out.DocumentStoragePort;
import com.oussama.sovereignty.infrastructure.adapters.out.storage.LocalDocumentStorage;
import com.oussama.sovereignty.infrastructure.adapters.out.storage.S3DocumentStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.nio.file.Path;

@Configuration
public class DocumentStorageConfig {

    @Value("${sovereignty.storage.path}")
    private String path;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "sovereignty.storage.provider", havingValue = "local")
    DocumentStoragePort localDocumentStorage() {
        return new LocalDocumentStorage(Path.of(path));
    }

    @Bean
    @ConditionalOnProperty(name = "sovereignty.storage", havingValue = "s3")
    DocumentStoragePort s3DocumentStorage() {
        return new S3DocumentStorage();
    }

}
