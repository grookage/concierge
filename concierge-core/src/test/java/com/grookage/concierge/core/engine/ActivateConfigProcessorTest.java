package com.grookage.concierge.core.engine;

import com.grookage.concierge.core.engine.processors.ActivateConfigProcessor;
import com.grookage.concierge.core.engine.resolver.DefaultConfigVersionManager;
import com.grookage.concierge.core.utils.ContextUtils;
import com.grookage.concierge.models.ResourceHelper;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.config.ConfigState;
import com.grookage.concierge.models.exception.ConciergeException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

class ActivateConfigProcessorTest extends AbstractProcessorTest {
    @Override
    public ConciergeProcessor createConciergeProcessor() {
        return ActivateConfigProcessor.builder()
                .processorFactory(this.getProcessorFactory())
                .configVersionManager(new DefaultConfigVersionManager())
                .repositorySupplier(this::getConciergeRepository)
                .build();
    }

    @Test
    @SneakyThrows
    void testActivateConfigNotInApprovedState() {
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
        Mockito.when(getConciergeRepository().getStoredRecord(configKey))
                .thenReturn(Optional.of(configDetails));
        Assertions.assertThrows(ConciergeException.class, () -> processor.process(conciergeContext));
        Mockito.verify(getConciergeRepository(), Mockito.times(0)).update(configDetails);
    }

    @Test
    @SneakyThrows
    void testActivateConfig() {
        final var conciergeContext = new ConciergeContext();
        final var configKey = ResourceHelper.getResource("configKey.json",
                ConfigKey.class);
        conciergeContext.addContext(ConfigKey.class.getSimpleName(), configKey);
        ContextUtils.addConfigUpdaterContext(conciergeContext, getConfigUpdater());
        final var configDetails = ResourceHelper.getResource("configDetails.json", ConfigDetails.class);
        Mockito.when(getConciergeRepository().getStoredRecord(configKey))
                .thenReturn(Optional.of(configDetails));
        configDetails.setConfigState(ConfigState.APPROVED);
        getConciergeProcessor().process(conciergeContext);
        Mockito.verify(getConciergeRepository(), Mockito.times(1)).rollOverAndUpdate(configDetails);
    }
}
