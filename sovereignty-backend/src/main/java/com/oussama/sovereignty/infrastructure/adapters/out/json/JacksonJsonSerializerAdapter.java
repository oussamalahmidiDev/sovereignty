package com.oussama.sovereignty.infrastructure.adapters.out.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oussama.sovereignty.application.ports.out.JsonSerializerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.UncheckedIOException;

@Component
@RequiredArgsConstructor
public class JacksonJsonSerializerAdapter implements JsonSerializerPort {

    private final ObjectMapper objectMapper;

    @Override
    public String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    @Override
    public <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to " + clazz.getSimpleName(), e);
        }
    }
}
