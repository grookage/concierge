package com.grookage.concierge.core.engine.processors;

import com.grookage.concierge.core.engine.ConciergeContext;
import com.grookage.concierge.core.engine.ConciergeProcessor;
import com.grookage.concierge.core.exception.ConciergeErrorCode;
import com.grookage.concierge.core.exception.ConciergeException;
import com.grookage.concierge.core.managers.VersionGenerator;
import com.grookage.concierge.core.utils.ConfigurationUtils;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.config.ConfigState;
import com.grookage.concierge.models.ingestion.ConfigurationRequest;
import com.grookage.concierge.repository.ConciergeRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.function.Supplier;

@AllArgsConstructor
@Slf4j
@Getter
public class CreateConfigProcessor extends ConciergeProcessor {

    private final Supplier<ConciergeRepository> repositorySupplier;
    private final Supplier<VersionGenerator> versionSupplier;

    @Override
    public ConfigEvent name() {
        return ConfigEvent.CREATE_CONFIG;
    }

    @Override
    @SneakyThrows
    public void process(ConciergeContext context) {
        final var createConfigRequest = context.getContext(ConfigurationRequest.class)
                .orElseThrow((Supplier<Throwable>) () -> ConciergeException.error(ConciergeErrorCode.VALUE_NOT_FOUND));
        final var storedConfigs = getRepositorySupplier()
                .get()
                .getRecords(createConfigRequest.getNamespace(), Set.of(createConfigRequest.getConfigName()));
        if (!storedConfigs.isEmpty() && storedConfigs.stream()
                .anyMatch(each -> each.getConfigState() == ConfigState.CREATED)) {
            log.error("There are already stored configs present with namespace {} and configName {}. Please try updating them instead",
                    createConfigRequest.getNamespace(), createConfigRequest.getConfigName());
            throw ConciergeException.error(ConciergeErrorCode.CONFIG_ALREADY_EXISTS);
        }
        final var configDetails = ConfigurationUtils.toCreateConfigRequest(createConfigRequest,
                getVersionSupplier().get()
        );
        addHistory(context, configDetails);
        getRepositorySupplier().get().save(configDetails);
        context.addContext(ConfigDetails.class.getSimpleName(), configDetails);
    }
}
