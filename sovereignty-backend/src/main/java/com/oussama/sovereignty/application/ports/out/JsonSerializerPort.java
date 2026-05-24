package com.oussama.sovereignty.application.ports.out;

public interface JsonSerializerPort {
    String serialize(Object obj);
    <T> T deserialize(String json, Class<T> clazz);
}
