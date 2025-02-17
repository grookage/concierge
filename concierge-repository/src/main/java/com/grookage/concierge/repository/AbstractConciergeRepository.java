package com.grookage.concierge.repository;

import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.config.ConfigState;
import com.grookage.concierge.repository.cache.CacheConfig;
import com.grookage.concierge.repository.cache.RepositoryRefresher;
import com.grookage.concierge.repository.cache.RepositorySupplier;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
public abstract class AbstractConciergeRepository implements ConciergeRepository {

    private final CacheConfig cacheConfig;
    private RepositoryRefresher refresher;

    protected AbstractConciergeRepository(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        if (null != cacheConfig && cacheConfig.isEnabled()) {
            this.refresher = RepositoryRefresher.builder()
                    .supplier(new RepositorySupplier(this))
                    .dataRefreshInterval(cacheConfig.getRefreshCacheSeconds())
                    .periodicRefresh(true)
                    .build();
        }
    }

    private Optional<ConfigDetails> getLatestActiveRecord(final String namespace, final String configName){
        if (null == cacheConfig || !cacheConfig.isEnabled()) {
            return getStoredRecords(namespace, Set.of(configName), Set.of(ConfigState.ACTIVATED)).stream()
                    .max(Comparator.naturalOrder()).stream().findFirst();
        }
        return refresher.getData().getConfigs().stream().filter(
                        each -> each.getConfigKey().getReferenceTag().equalsIgnoreCase(each.getConfigKey().getReferenceTag())
                                && each.getConfigState() == ConfigState.ACTIVATED)
                .max(Comparator.naturalOrder()).stream().findFirst();
    }

    @Override
    public Optional<ConfigDetails> getRecord(ConfigKey configKey) {
        if (configKey.latest()) {
            return getLatestActiveRecord(configKey.getNamespace(), configKey.getConfigName());
        }
        return cacheConfig.isEnabled() ? refresher.getData().getConfiguration(configKey) :
                getStoredRecord(configKey);
    }

    public abstract List<ConfigDetails> getStoredRecords(String namespace,
                                                         Set<String> configNames,
                                                         Set<ConfigState> configStates);

    @Override
    public List<ConfigDetails> getRecords(String namespace, Set<String> configNames) {
        final var configStates = Arrays.stream(ConfigState.values()).collect(Collectors.toSet());
        if (null == cacheConfig || !cacheConfig.isEnabled()) {
            return getStoredRecords(namespace, configNames, configStates);
        }

        final var configs = refresher.getData().getConfigs();
        return configs.stream().filter(
                        each -> each.getConfigKey().getNamespace().equalsIgnoreCase(namespace)
                                && configNames.contains(each.getConfigKey().getConfigName()))
                .toList();
    }

    public abstract List<ConfigDetails> getActiveStoredRecords(Set<String> namespaces);

    public List<ConfigDetails> getActiveRecords(final Set<String> namespaces) {
        if (null == cacheConfig || !cacheConfig.isEnabled()) {
            return getActiveStoredRecords(namespaces);
        }

        final var configs = refresher.getData().getConfigs();
        return configs.stream()
                .filter(each -> namespaces.contains(each.getConfigKey().getNamespace()) &&
                        each.getConfigState() == ConfigState.ACTIVATED)
                .toList();
    }

    public abstract List<ConfigDetails> getStoredRecords(Set<String> namespaces);

    @Override
    public List<ConfigDetails> getRecords(Set<String> namespaces) {
        if (null == cacheConfig || !cacheConfig.isEnabled()) {
            return getStoredRecords(namespaces);
        }
        final var configs = refresher.getData().getConfigs();
        return configs.stream()
                .filter(each -> namespaces.contains(each.getConfigKey().getNamespace()))
                .toList();
    }

    @Override
    public List<ConfigDetails> getRecords() {
        if (null == cacheConfig || !cacheConfig.isEnabled()) {
            return getStoredRecords();
        }
        return refresher.getData().getConfigs().stream().toList();
    }
}
