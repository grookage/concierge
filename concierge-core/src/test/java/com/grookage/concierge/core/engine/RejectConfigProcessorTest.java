package com.grookage.concierge.core.engine;

import com.grookage.concierge.core.engine.processors.RejectConfigProcessor;
import com.grookage.concierge.core.utils.ContextUtils;
import com.grookage.concierge.models.ResourceHelper;
import com.grookage.concierge.models.config.*;
import com.grookage.concierge.models.exception.ConciergeException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

class RejectConfigProcessorTest extends AbstractProcessorTest {
    @Override
    public ConciergeProcessor createConciergeProcessor() {
        return RejectConfigProcessor.builder()
                .processorFactory(this.getProcessorFactory())
                .repositorySupplier(this::getConciergeRepository)
                .build();
    }

    @Test
    @SneakyThrows
    void testRejectConfigInActivatedState() {
        final var conciergeContext = new ConciergeContext();
        final var configKey = ResourceHelper.getResource("configKey.json",
                ConfigKey.class);
        conciergeContext.addContext(ConfigKey.class.getSimpleName(), configKey);
        ContextUtils.addConfigUpdaterContext(conciergeContext, getConfigUpdater());
        Mockito.when(getConciergeRepository().getStoredRecord(configKey))
                .thenReturn(Optional.empty());
        final var processor = getConciergeProcessor();
        Assertions.assertThrows(ConciergeException.class, () -> processor.process(conciergeContext));
        final var configDetails = ResourceHelper.getResource("configDetails.json", ConfigDetails.class);
        configDetails.addHistory(
                ConfigHistoryItem.builder()
                        .configEvent(ConfigEvent.CREATE_CONFIG)
                        .configUpdaterId("updaterId")
                        .build()
        );
        configDetails.setConfigState(ConfigState.ACTIVATED);
        Mockito.when(getConciergeRepository().getStoredRecord(configKey))
                .thenReturn(Optional.of(configDetails));
        getConciergeProcessor().process(conciergeContext);
        Mockito.verify(getConciergeRepository(), Mockito.times(1)).update(configDetails);
    }

    @Test
    @SneakyThrows
    void testRejectConfiguration() {
        final var conciergeContext = new ConciergeContext();
        final var configKey = ResourceHelper.getResource("configKey.json",
                ConfigKey.class);
        conciergeContext.addContext(ConfigKey.class.getSimpleName(), configKey);
        ContextUtils.addConfigUpdaterContext(conciergeContext, getConfigUpdater());
        final var configDetails = ResourceHelper.getResource("configDetails.json", ConfigDetails.class);
        configDetails.addHistory(
                ConfigHistoryItem.builder()
                        .configEvent(ConfigEvent.CREATE_CONFIG)
                        .configUpdaterId("updaterId")
                        .build()
        );
        Mockito.when(getConciergeRepository().getStoredRecord(configKey))
                .thenReturn(Optional.of(configDetails));
        getConciergeProcessor().process(conciergeContext);
        Mockito.verify(getConciergeRepository(), Mockito.times(1)).update(configDetails);
    }
}
