package com.grookage.concierge.core.services.impl;

import com.grookage.concierge.core.engine.ConciergeContext;
import com.grookage.concierge.core.engine.ConciergeHub;
import com.grookage.concierge.core.exception.ConciergeErrorCode;
import com.grookage.concierge.core.exception.ConciergeException;
import com.grookage.concierge.core.managers.ProcessorFactory;
import com.grookage.concierge.core.services.IngestionService;
import com.grookage.concierge.core.utils.ContextUtils;
import com.grookage.concierge.models.ConfigUpdater;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.ingestion.ConfigurationRequest;
import com.grookage.concierge.models.ingestion.UpdateConfigRequest;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@AllArgsConstructor
@Slf4j
public class IngestionServiceImpl<C extends ConfigUpdater> implements IngestionService<C> {

    private final Supplier<ProcessorFactory> processorFactory;
    private final ConciergeHub conciergeHub;

    @Override
    @SneakyThrows
    public ConfigDetails createConfiguration(C configUpdater, ConfigurationRequest configurationRequest) {
        final var conciergeContext = new ConciergeContext();
        conciergeContext.addContext(ConfigurationRequest.class.getSimpleName(), configurationRequest);
        ContextUtils.addSchemaUpdaterContext(conciergeContext, configUpdater);
        final var processor = conciergeHub.getProcessor(ConfigEvent.CREATE_CONFIG)
                .orElseThrow((Supplier<Throwable>) () -> ConciergeException.error(ConciergeErrorCode.PROCESSOR_NOT_FOUND));
        processor.fire(conciergeContext, processorFactory.get());
        return conciergeContext.getContext(ConfigDetails.class).orElse(null);
    }

    @Override
    @SneakyThrows
    public ConfigDetails updateConfiguration(C configUpdater, UpdateConfigRequest configurationRequest) {
        final var conciergeContext = new ConciergeContext();
        conciergeContext.addContext(UpdateConfigRequest.class.getSimpleName(), configurationRequest);
        ContextUtils.addSchemaUpdaterContext(conciergeContext, configUpdater);
        final var processor = conciergeHub.getProcessor(ConfigEvent.UPDATE_CONFIG)
                .orElseThrow((Supplier<Throwable>) () -> ConciergeException.error(ConciergeErrorCode.PROCESSOR_NOT_FOUND));
        processor.fire(conciergeContext, processorFactory.get());
        return conciergeContext.getContext(ConfigDetails.class).orElse(null);
    }

    @Override
    @SneakyThrows
    public ConfigDetails approveConfiguration(C configUpdater, ConfigKey configKey) {
        final var conciergeContext = new ConciergeContext();
        conciergeContext.addContext(ConfigKey.class.getSimpleName(), configKey);
        ContextUtils.addSchemaUpdaterContext(conciergeContext, configUpdater);
        final var processor = conciergeHub.getProcessor(ConfigEvent.APPROVE_CONFIG)
                .orElseThrow((Supplier<Throwable>) () -> ConciergeException.error(ConciergeErrorCode.PROCESSOR_NOT_FOUND));
        processor.fire(conciergeContext, processorFactory.get());
        return conciergeContext.getContext(ConfigDetails.class).orElse(null);
    }

    @Override
    @SneakyThrows
    public ConfigDetails rejectConfiguration(C configUpdater, ConfigKey configKey) {
        final var conciergeContext = new ConciergeContext();
        conciergeContext.addContext(ConfigKey.class.getSimpleName(), configKey);
        ContextUtils.addSchemaUpdaterContext(conciergeContext, configUpdater);
        final var processor = conciergeHub.getProcessor(ConfigEvent.REJECT_CONFIG)
                .orElseThrow((Supplier<Throwable>) () -> ConciergeException.error(ConciergeErrorCode.PROCESSOR_NOT_FOUND));
        processor.fire(conciergeContext, processorFactory.get());
        return conciergeContext.getContext(ConfigDetails.class).orElse(null);
    }

    @Override
    @SneakyThrows
    public ConfigDetails activateConfiguration(C configUpdater, ConfigKey configKey) {
        final var conciergeContext = new ConciergeContext();
        conciergeContext.addContext(ConfigKey.class.getSimpleName(), configKey);
        ContextUtils.addSchemaUpdaterContext(conciergeContext, configUpdater);
        final var processor = conciergeHub.getProcessor(ConfigEvent.ACTIVATE_CONFIG)
                .orElseThrow((Supplier<Throwable>) () -> ConciergeException.error(ConciergeErrorCode.PROCESSOR_NOT_FOUND));
        processor.fire(conciergeContext, processorFactory.get());
        return conciergeContext.getContext(ConfigDetails.class).orElse(null);
    }
}
