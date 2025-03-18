package com.grookage.concierge.core.services.impl;

import com.grookage.concierge.core.cache.CacheConfig;
import com.grookage.concierge.core.cache.RepositoryRefresher;
import com.grookage.concierge.core.cache.RepositorySupplier;
import com.grookage.concierge.core.services.ConfigService;
import com.grookage.concierge.models.CollectionUtils;
import com.grookage.concierge.models.SearchRequest;
import com.grookage.concierge.models.config.ConciergeRequestContext;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.config.ConfigState;
import com.grookage.concierge.repository.ConciergeRepository;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Slf4j
public class ConfigServiceImpl implements ConfigService {

    private final Supplier<ConciergeRepository> repositorySupplier;
    private final CacheConfig cacheConfig;
    private RepositoryRefresher refresher;

    @Builder
    public ConfigServiceImpl(Supplier<ConciergeRepository> repositorySupplier, CacheConfig cacheConfig) {
        this.repositorySupplier = repositorySupplier;
        this.cacheConfig = cacheConfig;
        if (null != cacheConfig && cacheConfig.isEnabled()) {
            final var supplier = new RepositorySupplier(repositorySupplier);
            supplier.start();
            this.refresher = RepositoryRefresher.builder()
                    .supplier(supplier)
                    .dataRefreshInterval(cacheConfig.getRefreshCacheSeconds())
                    .periodicRefresh(true)
                    .build();
            refresher.start();
        }
    }

    private boolean useRepositoryCache(final ConciergeRequestContext requestContext) {
        return null != requestContext && !requestContext.isIgnoreCache()
                && null != cacheConfig && cacheConfig.isEnabled();
    }

    @Override
    public Optional<ConfigDetails> getConfig(ConciergeRequestContext requestContext, ConfigKey configKey) {
        final var useCache = useRepositoryCache(requestContext);
        if (configKey.latest()) {
            return useCache ?
                    refresher.getData()
                            .getConfigs()
                            .stream().filter(
                                    each -> each.getConfigKey().getReferenceTag().equalsIgnoreCase(each.getConfigKey().getReferenceTag())
                                            && each.getConfigState() == ConfigState.ACTIVATED
                            )
                            .max(Comparator.naturalOrder()).stream().findFirst()
                    : repositorySupplier.get()
                    .getStoredRecords(Set.of(configKey.getNamespace()), Set.of(configKey.getConfigName()), Set.of(ConfigState.ACTIVATED))
                    .stream()
                    .max(Comparator.naturalOrder()).stream().findFirst();

        }
        return useCache ? refresher.getData().getConfiguration(configKey) :
                repositorySupplier.get().getStoredRecord(configKey);
    }

    @Override
    public Optional<ConfigDetails> getLatestActiveConfig(ConciergeRequestContext requestContext, String namespace, String configName) {
        final var configKey = ConfigKey.builder()
                .namespace(namespace)
                .configName(configName)
                .version("latest")
                .build();
        return getConfig(requestContext, configKey);
    }

    private boolean match(ConfigDetails configDetails, SearchRequest searchRequest) {
        final var configKey = configDetails.getConfigKey();
        final var namespaceMatch = CollectionUtils.isNullOrEmpty(searchRequest.getNamespaces()) ||
                searchRequest.getNamespaces().contains(configKey.getNamespace());
        final var configNameMatch = CollectionUtils.isNullOrEmpty(searchRequest.getConfigNames()) ||
                searchRequest.getConfigNames().contains(configKey.getConfigName());
        final var configStateMatch = CollectionUtils.isNullOrEmpty(searchRequest.getConfigStates()) ||
                searchRequest.getConfigStates().contains(configDetails.getConfigState());
        return namespaceMatch && configNameMatch && configStateMatch;
    }

    @Override
    public List<ConfigDetails> getConfigs(ConciergeRequestContext requestContext, SearchRequest searchRequest) {
        if (!useRepositoryCache(requestContext)) {
            return repositorySupplier.get().getStoredRecords(searchRequest.getNamespaces(),
                    searchRequest.getConfigNames(), searchRequest.getConfigStates());
        }
        return refresher.getData().getConfigs().stream()
                .filter(each -> match(each, searchRequest))
                .toList();
    }

    @Override
    public List<ConfigDetails> getActiveConfigs(ConciergeRequestContext requestContext, Set<String> namespaces) {
        if (!useRepositoryCache(requestContext)) {
            return repositorySupplier.get().getActiveStoredRecords(namespaces);
        }
        return refresher.getData().getConfigs()
                .stream()
                .filter(each -> (CollectionUtils.isNullOrEmpty(namespaces) || namespaces.contains(each.getConfigKey().getNamespace()))
                        && each.getConfigState() == ConfigState.ACTIVATED)
                .toList();
    }

    @Override
    public List<ConfigDetails> getConfigs(ConciergeRequestContext requestContext) {
        return useRepositoryCache(requestContext) ? refresher.getData().getConfigs().stream().toList()
                : repositorySupplier.get().getStoredRecords();
    }
}
