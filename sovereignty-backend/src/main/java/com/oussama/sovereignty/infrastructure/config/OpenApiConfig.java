package com.oussama.sovereignty.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sovereigntyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sovereignty AI API")
                        .description("API Documentation for Sovereignty AI - A local document search and retrieval system (RAG).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sovereignty AI Team")
                                .email("contact@sovereignty.ai")));
    }
}
