package com.grookage.concierge.core.engine;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.grookage.concierge.models.MapperUtils;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.Optional;

@NoArgsConstructor
public class ConciergeContext {
    private final ContextData data = new ContextData();

    @JsonIgnore
    public <V> void addContext(String key, V value) {
        if (Strings.isNullOrEmpty(key.toUpperCase())) {
            throw new IllegalArgumentException("Invalid key for context data. Key cannot be null/empty");
        }
        this.data.put(key.toUpperCase(), value);
    }

    @SneakyThrows
    public <T> Optional<T> getContext(Class<T> klass) {
        var value = this.data.get(klass.getSimpleName().toUpperCase());
        return Optional.ofNullable(MapperUtils.mapper().convertValue(value, klass));
    }

    @SneakyThrows
    public Optional<String> getValue(String key) {
        var value = this.data.get(key.toUpperCase());
        return Optional.ofNullable((String) value);
    }

}
