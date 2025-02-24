package com.grookage.concierge.core.services;

import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ConfigService {

    Optional<ConfigDetails> getConfig(ConfigKey configKey);

    Optional<ConfigDetails> getLatestActiveConfig(final String namespace, final String configName);

    List<ConfigDetails> getConfigs(Set<String> namespaces);

    List<ConfigDetails> getConfigs(String namespace, Set<String> configNames);

    List<ConfigDetails> getActiveConfigs(Set<String> namespaces);

    List<ConfigDetails> getConfigs();

}
