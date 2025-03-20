package com.grookage.concierge.core.cache;

import com.grookage.concierge.models.config.ConfigDetails;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ConfigRegistry {

    private final ConcurrentHashMap<String, ConfigDetails> schemas = new ConcurrentHashMap<>();

    public void add(final ConfigDetails configDetails) {
        schemas.putIfAbsent(configDetails.getConfigKey().getReferenceId(), configDetails);
    }

    public Collection<ConfigDetails> getConfigs() {
        return schemas.values();
    }

    public Optional<ConfigDetails> getConfiguration(final String referenceId) {
        return Optional.ofNullable(schemas.get(referenceId));
    }

}
