package com.grookage.concierge.core.engine;

import com.grookage.concierge.core.engine.processors.UpdateConfigProcessor;
import com.grookage.concierge.core.utils.ContextUtils;
import com.grookage.concierge.models.ResourceHelper;
import com.grookage.concierge.models.config.*;
import com.grookage.concierge.models.exception.ConciergeException;
import com.grookage.concierge.models.ingestion.UpdateConfigRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

class UpdateConfigProcessorTest extends AbstractProcessorTest {
    @Override
    public ConciergeProcessor createConciergeProcessor() {
        return UpdateConfigProcessor.builder()
                .processorFactory(this.getProcessorFactory())
                .repositorySupplier(this::getConciergeRepository)
                .build();
    }

    @Test
    @SneakyThrows
    void testUpdateConfigNoCreatedRecord() {
        final var conciergeContext = new ConciergeContext();
        ContextUtils.addConfigUpdaterContext(conciergeContext, getConfigUpdater());
        final var updateConfigRequest = ResourceHelper.getResource("updateConfiguration.json", UpdateConfigRequest.class);
        conciergeContext.addContext(UpdateConfigRequest.class.getSimpleName(), updateConfigRequest);
        final var processor = getConciergeProcessor();
        Mockito.when(getConciergeRepository().getStoredRecord(Mockito.any(ConfigKey.class)))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(ConciergeException.class, () -> processor.process(conciergeContext));
    }

    @Test
    @SneakyThrows
    void testUpdateConfig() {
        final var conciergeContext = new ConciergeContext();
        ContextUtils.addConfigUpdaterContext(conciergeContext, getConfigUpdater());
        final var updateConfigRequest = ResourceHelper.getResource("updateConfiguration.json", UpdateConfigRequest.class);
        conciergeContext.addContext(UpdateConfigRequest.class.getSimpleName(), updateConfigRequest);
        final var processor = getConciergeProcessor();
        final var configDetails = ResourceHelper.getResource("configDetails.json", ConfigDetails.class);
        configDetails.addHistory(
                ConfigHistoryItem.builder()
                        .configEvent(ConfigEvent.CREATE_CONFIG)
                        .configUpdaterId("updaterId")
                        .build()
        );
        Mockito.when(getConciergeRepository().getStoredRecord(Mockito.any(ConfigKey.class)))
                .thenReturn(Optional.of(configDetails));
        processor.process(conciergeContext);
        Mockito.verify(getConciergeRepository(), Mockito.times(1))
                .update(Mockito.any(ConfigDetails.class));
    }

    @Test
    @SneakyThrows
    void testUpdateConfigNotCreatedState() {
        final var conciergeContext = new ConciergeContext();
        ContextUtils.addConfigUpdaterContext(conciergeContext, getConfigUpdater());
        final var updateConfigRequest = ResourceHelper.getResource("updateConfiguration.json", UpdateConfigRequest.class);
        conciergeContext.addContext(UpdateConfigRequest.class.getSimpleName(), updateConfigRequest);
        final var processor = getConciergeProcessor();
        final var configDetails = ResourceHelper.getResource("configDetails.json", ConfigDetails.class);
        Mockito.when(getConciergeRepository().getStoredRecord(Mockito.any(ConfigKey.class)))
                .thenReturn(Optional.of(configDetails));
        configDetails.setConfigState(ConfigState.ACTIVATED);
        Assertions.assertThrows(ConciergeException.class, () -> processor.process(conciergeContext));
    }
}
