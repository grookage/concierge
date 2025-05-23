package com.grookage.concierge.core.engine.processors;

import com.grookage.concierge.core.engine.ConciergeContext;
import com.grookage.concierge.core.engine.ConciergeProcessor;
import com.grookage.concierge.core.utils.ConfigurationUtils;
import com.grookage.concierge.models.SearchRequest;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.config.ConfigState;
import com.grookage.concierge.models.exception.ConciergeCoreErrorCode;
import com.grookage.concierge.models.exception.ConciergeException;
import com.grookage.concierge.models.ingestion.ConfigurationRequest;
import com.grookage.concierge.repository.ConciergeRepository;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.function.Supplier;

@SuperBuilder
@Slf4j
@Getter
public class CreateConfigProcessor extends ConciergeProcessor {

    private final Supplier<ConciergeRepository> repositorySupplier;

    @Override
    public ConfigEvent name() {
        return ConfigEvent.CREATE_CONFIG;
    }

    //Don't allow the same version config to be created again,
    //Don't allow more than one created config
    @Override
    @SneakyThrows
    public void process(ConciergeContext context) {
        final var createConfigRequest = context.getContext(ConfigurationRequest.class)
                .orElseThrow((Supplier<Throwable>) () -> ConciergeException.error(ConciergeCoreErrorCode.VALUE_NOT_FOUND));
        final var configKey = createConfigRequest.getConfigKey();
        final var searchRequest = SearchRequest.builder()
                .orgs(Set.of(configKey.getOrgId()))
                .tenants(Set.of(configKey.getTenantId()))
                .namespaces(Set.of(configKey.getNamespace()))
                .configNames(Set.of(configKey.getConfigName()))
                .build();
        final var storedConfigs = getRepositorySupplier()
                .get()
                .getStoredRecords(searchRequest);
        final var anyCreatedOrMatchingVersionConfig = storedConfigs.stream()
                .anyMatch(each -> each.getConfigState() == ConfigState.CREATED ||
                        each.getConfigKey().getVersion().equalsIgnoreCase(configKey.getVersion()));
        if (anyCreatedOrMatchingVersionConfig) {
            log.error("There are already stored configs present with configMeta {}. Please try updating them instead",
                    createConfigRequest.getConfigKey());
            throw ConciergeException.error(ConciergeCoreErrorCode.CONFIG_ALREADY_EXISTS);
        }
        final var configDetails = ConfigurationUtils.toConfigDetails(createConfigRequest);
        addHistory(context, configDetails, createConfigRequest.getMessage());
        getRepositorySupplier().get().create(configDetails);
        context.addContext(ConfigDetails.class.getSimpleName(), configDetails);
    }
}
