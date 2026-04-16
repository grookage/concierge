package com.grookage.concierge.core.services.impl;

import com.grookage.concierge.core.cache.CacheConfig;
import com.grookage.concierge.core.cache.ConfigRegistry;
import com.grookage.concierge.core.cache.RepositoryRefresher;
import com.grookage.concierge.core.cache.RepositorySupplier;
import com.grookage.concierge.core.services.ConfigService;
import com.grookage.concierge.models.SearchRequest;
import com.grookage.concierge.models.config.ConciergeRequestContext;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.repository.ConciergeRepository;
import com.grookage.korg.consumer.KorgConsumer;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ConfigServiceImpl implements ConfigService {

    private final Supplier<ConciergeRepository> repositorySupplier;
    private final CacheConfig cacheConfig;
    private RepositoryRefresher refresher;

    @Builder
    public ConfigServiceImpl(Supplier<ConciergeRepository> repositorySupplier,
                             CacheConfig cacheConfig,
                             Supplier<KorgConsumer<ConfigRegistry>> consumerSupplier) {
        this.repositorySupplier = repositorySupplier;
        this.cacheConfig = cacheConfig;
        if (null != cacheConfig && cacheConfig.isEnabled()) {
            final var supplier = new RepositorySupplier(repositorySupplier, cacheConfig.getConfigTypes());
            supplier.start();
            this.refresher = RepositoryRefresher.builder()
                    .supplier(supplier)
                    .dataRefreshInterval(cacheConfig.getRefreshCacheSeconds())
                    .periodicRefresh(true)
                    .consumerSupplier(consumerSupplier)
                    .build();
            refresher.start();
        }
    }

    @Override
    public Optional<ConfigDetails> getConfig(ConciergeRequestContext requestContext, String configType, String referenceId) {
        final var useCache = useRepositoryCache(requestContext, configType);
        return useCache ? refresher.getData().getConfigDetails(configType, referenceId) :
                repositorySupplier.get().getStoredRecord(configType, referenceId);
    }

    @Override
    public Optional<ConfigDetails> getConfig(ConciergeRequestContext requestContext, ConfigKey configKey) {
        return getConfig(requestContext, configKey.getConfigType(), configKey.getReferenceId());
    }

    @Override
    public List<ConfigDetails> getConfigs(ConciergeRequestContext requestContext, SearchRequest searchRequest) {
        if (!cacheEnabled(requestContext)) {
            log.debug("There is no cache enabled for the requestContext, fetching the configs directly from the repository directly");
            return repositorySupplier.get().getStoredRecords(searchRequest);
        }

        var partitioned = searchRequest.getConfigTypes().stream()
                .collect(Collectors.partitioningBy(cacheConfig::cachedType, Collectors.toSet()));
        var cachedTypes = partitioned.get(true);
        var nonCachedTypes = partitioned.get(false);

        var cacheFuture = cachedTypes.isEmpty()
                ? CompletableFuture.completedFuture(List.<ConfigDetails>of())
                : CompletableFuture.supplyAsync(() ->
                refresher.getData().getMatchingConfigs(searchRequest.toBuilder().configTypes(cachedTypes).build()));

        var repoFuture = nonCachedTypes.isEmpty()
                ? CompletableFuture.completedFuture(List.<ConfigDetails>of())
                : CompletableFuture.supplyAsync(() ->
                repositorySupplier.get().getStoredRecords(searchRequest.toBuilder().configTypes(nonCachedTypes).build()));

        return cacheFuture.thenCombine(repoFuture, (cached, repo) ->
                Stream.concat(cached.stream(), repo.stream()).toList()
        ).join();
    }

    @Override
    public Optional<Consumer<ConfigRegistry>> getConfigConsumer(ConciergeRequestContext requestContext) {
        if (!cacheEnabled(requestContext)) {
            log.debug("There is no cache context enabled, returning the empty config consumer");
            return Optional.empty();
        }
        return Optional.ofNullable(refresher.getConsumerSupplier()).map(Supplier::get);
    }

    private boolean cacheEnabled(final ConciergeRequestContext requestContext) {
        return null != requestContext && !requestContext.isIgnoreCache()
                && null != cacheConfig && cacheConfig.isEnabled()
                && null != refresher;
    }

    private boolean useRepositoryCache(final ConciergeRequestContext requestContext, final String configType) {
        return null != requestContext && !requestContext.isIgnoreCache()
                && null != cacheConfig && cacheConfig.isEnabled()
                && cacheConfig.cachedType(configType)
                && null != refresher;
    }
}
