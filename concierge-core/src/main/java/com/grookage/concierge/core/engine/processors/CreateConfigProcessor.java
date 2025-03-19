package com.grookage.concierge.core.engine.processors;

import com.grookage.concierge.core.engine.ConciergeContext;
import com.grookage.concierge.core.engine.ConciergeProcessor;
import com.grookage.concierge.core.utils.ConfigurationUtils;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.exception.ConciergeCoreErrorCode;
import com.grookage.concierge.models.exception.ConciergeException;
import com.grookage.concierge.models.ingestion.ConfigurationRequest;
import com.grookage.concierge.repository.ConciergeRepository;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

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

    @Override
    @SneakyThrows
    public void process(ConciergeContext context) {
        final var createConfigRequest = context.getContext(ConfigurationRequest.class)
                .orElseThrow((Supplier<Throwable>) () -> ConciergeException.error(ConciergeCoreErrorCode.VALUE_NOT_FOUND));
        final var recordExists = getRepositorySupplier()
                .get()
                .createdRecordExists(createConfigRequest.getNamespace(), createConfigRequest.getConfigName());
        if (recordExists) {
            log.error("There are already stored configs present with namespace {} and configName {}. Please try updating them instead",
                    createConfigRequest.getNamespace(), createConfigRequest.getConfigName());
            throw ConciergeException.error(ConciergeCoreErrorCode.CONFIG_ALREADY_EXISTS);
        }
        final var configDetails = ConfigurationUtils.toConfigDetails(createConfigRequest);
        addHistory(context, configDetails, createConfigRequest.getMessage());
        getRepositorySupplier().get().create(configDetails);
        context.addContext(ConfigDetails.class.getSimpleName(), configDetails);
    }
}
