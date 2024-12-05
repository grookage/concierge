package com.grookage.concierge.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.IOException;

public class ResourceHelper {
    @Getter
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T getResource(String path, Class<T> klass) throws IOException {
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        final var data = ResourceHelper.class.getClassLoader().getResourceAsStream(path);
        return objectMapper.readValue(data, klass);
    }

    public static <T> T getResource(String path, TypeReference<T> klass) throws IOException {
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        final var data = ResourceHelper.class.getClassLoader().getResourceAsStream(path);
        return objectMapper.readValue(data, klass);
    }
}
