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
import com.grookage.concierge.repository.ConciergeRepository;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
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

    @Override
    public Optional<ConfigDetails> getConfig(ConciergeRequestContext requestContext, String referenceId) {
        final var useCache = useRepositoryCache(requestContext);
        return useCache ? refresher.getData().getConfiguration(referenceId) :
                repositorySupplier.get().getStoredRecord(referenceId);
    }

    @Override
    public Optional<ConfigDetails> getConfig(ConciergeRequestContext requestContext, ConfigKey configKey) {
        return getConfig(requestContext, configKey.getReferenceId());
    }

    @Override
    public List<ConfigDetails> getConfigs(ConciergeRequestContext requestContext, SearchRequest searchRequest) {
        if (!useRepositoryCache(requestContext)) {
            return repositorySupplier.get().getStoredRecords(searchRequest);
        }
        return refresher.getData().getConfigs().stream()
                .filter(each -> match(each, searchRequest))
                .toList();
    }

    private boolean useRepositoryCache(final ConciergeRequestContext requestContext) {
        return null != requestContext && !requestContext.isIgnoreCache()
                && null != cacheConfig && cacheConfig.isEnabled();
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
}
