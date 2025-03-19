package com.grookage.concierge.core.engine.processors;

import com.grookage.concierge.core.engine.ConciergeContext;
import com.grookage.concierge.core.engine.ConciergeProcessor;
import com.grookage.concierge.models.MapperUtils;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.config.ConfigState;
import com.grookage.concierge.models.exception.ConciergeCoreErrorCode;
import com.grookage.concierge.models.exception.ConciergeException;
import com.grookage.concierge.models.ingestion.UpdateConfigRequest;
import com.grookage.concierge.repository.ConciergeRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@SuperBuilder
@Getter
@Slf4j
public class UpdateConfigProcessor extends ConciergeProcessor {

    private final Supplier<ConciergeRepository> repositorySupplier;

    @Override
    public ConfigEvent name() {
        return ConfigEvent.UPDATE_CONFIG;
    }

    @Override
    @SneakyThrows
    public void process(ConciergeContext context) {
        final var updateConfigRequest = context.getContext(UpdateConfigRequest.class)
                .orElseThrow((Supplier<Throwable>) () -> ConciergeException.error(ConciergeCoreErrorCode.VALUE_NOT_FOUND));
        final var storedConfig = getRepositorySupplier()
                .get()
                .getStoredRecord(ConfigKey.builder()
                        .version(updateConfigRequest.getVersion())
                        .configName(updateConfigRequest.getConfigName())
                        .namespace(updateConfigRequest.getNamespace())
                        .build()).orElse(null);
        if (null == storedConfig || storedConfig.getConfigState() != ConfigState.CREATED) {
            log.error("There are no stored configs present with namespace {}, version {} and configName {}. Please try updating them instead",
                    updateConfigRequest.getNamespace(),
                    updateConfigRequest.getVersion(),
                    updateConfigRequest.getConfigName());
            throw ConciergeException.error(ConciergeCoreErrorCode.NO_CONFIG_FOUND);
        }
        storedConfig.setDescription(updateConfigRequest.getDescription());
        storedConfig.setData(MapperUtils.mapper().writeValueAsBytes(updateConfigRequest.getData()));
        addHistory(context, storedConfig, updateConfigRequest.getMessage());
        getRepositorySupplier().get().update(storedConfig);
        context.addContext(ConfigDetails.class.getSimpleName(), storedConfig);
    }
}