package com.grookage.concierge.core.cache;

import com.grookage.concierge.models.CollectionUtils;
import com.grookage.concierge.models.SearchRequest;
import com.grookage.concierge.models.config.ConfigDetails;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class ConfigRegistry {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, ConfigDetails>> configSchemas = new ConcurrentHashMap<>();

    private String getCasedString(final String key) {
        return key.toUpperCase(Locale.ROOT);
    }

    public void add(final ConfigDetails configDetails) {
        configSchemas.computeIfAbsent(getCasedString(configDetails.getConfigKey().getConfigType()), k -> new ConcurrentHashMap<>())
                .putIfAbsent(configDetails.getConfigKey().getReferenceId(), configDetails);
    }

    public List<ConfigDetails> getMatchingConfigs(final SearchRequest searchRequest) {
        final var pageWindow = searchRequest.getPageWindow();
        return searchRequest.getConfigTypes().stream()
                .map(configType -> configSchemas.get(getCasedString(configType)))
                .filter(Objects::nonNull)
                .flatMap(configs -> configs.values().stream())
                .filter(config -> match(config, searchRequest))
                .skip(pageWindow.offset())
                .limit(pageWindow.getPageSize())
                .collect(Collectors.toList());
    }

    public Optional<ConfigDetails> getConfigDetails(final String configType, final String referenceId) {
        return Optional.ofNullable(configSchemas.get(getCasedString(configType)))
                .map(configs -> configs.get(referenceId));
    }

    private boolean match(ConfigDetails configDetails, SearchRequest searchRequest) {
        final var configKey = configDetails.getConfigKey();
        final var namespaceMatch = CollectionUtils.isNullOrEmpty(searchRequest.getNamespaces()) ||
                searchRequest.getNamespaces().contains(configKey.getNamespace());
        final var configNameMatch = CollectionUtils.isNullOrEmpty(searchRequest.getConfigNames()) ||
                searchRequest.getConfigNames().contains(configKey.getConfigName());
        final var configStateMatch = CollectionUtils.isNullOrEmpty(searchRequest.getConfigStates()) ||
                searchRequest.getConfigStates().contains(configDetails.getConfigState());
        final var orgMatch = CollectionUtils.isNullOrEmpty(searchRequest.getOrgs()) ||
                searchRequest.getOrgs().contains(configKey.getOrgId());
        final var tenantMatch = CollectionUtils.isNullOrEmpty(searchRequest.getTenants()) ||
                searchRequest.getTenants().contains(configKey.getTenantId());
        return namespaceMatch && configNameMatch && configStateMatch
                && orgMatch && tenantMatch;
    }

}
