package com.oussama.sovereignty.infrastructure.adapters.in.web;

import com.oussama.sovereignty.infrastructure.adapters.in.web.response.HealthStatusResponse;
import com.oussama.sovereignty.infrastructure.adapters.in.web.response.HealthStatusResponse.HealthStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private final DataSource dataSource;
    private final KafkaAdmin kafkaAdmin;

    @Value("${spring.ai.ollama.base-url}")
    private String ollamaUrl;

    public HealthStatusResponse checkHealth() {
        boolean ollamaOk = checkOllama();
        boolean dbOk = checkDatabase();
        boolean kafkaOk = checkKafka();

        boolean allOk = ollamaOk && dbOk && kafkaOk;
        HealthStatus status = allOk ? HealthStatus.UP : HealthStatus.DOWN;
        String message = allOk ? "Local engine is ready" : "Some services are not available. Check the logs";

        return new HealthStatusResponse(
                status,
                message,
                new HealthStatusResponse.HealthDetails(ollamaOk, dbOk, kafkaOk)
        );
    }

    private boolean checkOllama() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl + "/api/tags"))
                    .GET()
                    .timeout(Duration.ofSeconds(2))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.info("Ollama is healthy");
                return true;
            } else {
                log.warn("Ollama returned status {}", response.statusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Ollama health check failed", e);
            return false;
        }
    }

    private boolean checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            conn.isValid(2);
            log.info("Database is healthy");
            return true;
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return false;
        }
    }

    private boolean checkKafka() {
        try {
            var clusterId = kafkaAdmin.clusterId();
            log.info("Kafka is healthy. Cluster ID: {}", clusterId);
            return true;
        } catch (Exception e) {
            // Any exception indicates Kafka isn't healthy
            log.error("Kafka health check failed", e);
            return false;
        }
    }
}
