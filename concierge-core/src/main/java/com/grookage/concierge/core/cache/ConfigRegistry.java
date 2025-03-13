package com.grookage.concierge.core.cache;

import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ConfigRegistry {

    private final ConcurrentHashMap<ConfigKey, ConfigDetails> schemas = new ConcurrentHashMap<>();

    public void add(final ConfigDetails configDetails) {
        schemas.putIfAbsent(configDetails.getConfigKey(), configDetails);
    }

    public Collection<ConfigDetails> getConfigs() {
        return schemas.values();
    }

    public Optional<ConfigDetails> getConfiguration(final ConfigKey configKey) {
        return Optional.ofNullable(schemas.get(configKey));
    }

}
