package com.grookage.concierge.core.services.impl;

import com.grookage.concierge.core.services.ConfigService;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.repository.ConciergeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Slf4j
@AllArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final Supplier<ConciergeRepository> repositorySupplier;

    @Override
    public Optional<ConfigDetails> getConfig(ConfigKey configKey) {
        return repositorySupplier.get().getRecord(configKey);
    }

    @Override
    public List<ConfigDetails> getConfigs(Set<String> namespaces) {
        return repositorySupplier.get().getRecords(namespaces);
    }

    @Override
    public List<ConfigDetails> getConfigs(String namespace, Set<String> configNames) {
        return repositorySupplier.get().getRecords(namespace, configNames);
    }

    @Override
    public List<ConfigDetails> getActiveConfigs(Set<String> namespaces) {
        return repositorySupplier.get().getActiveRecords(namespaces);
    }

    @Override
    public List<ConfigDetails> getConfigs() {
        return repositorySupplier.get().getRecords();
    }

}
