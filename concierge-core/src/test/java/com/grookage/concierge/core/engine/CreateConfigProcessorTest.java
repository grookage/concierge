package com.grookage.concierge.core.engine;

import com.grookage.concierge.core.engine.processors.CreateConfigProcessor;
import com.grookage.concierge.core.utils.ConciergeTestUtils;
import com.grookage.concierge.core.utils.ContextUtils;
import com.grookage.concierge.models.ResourceHelper;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.exception.ConciergeException;
import com.grookage.concierge.models.ingestion.ConfigurationRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CreateConfigProcessorTest extends AbstractProcessorTest {

    @Override
    public ConciergeProcessor createConciergeProcessor() {
        return new CreateConfigProcessor(this::getConciergeRepository,
                () -> ConciergeTestUtils.generator);
    }


    @Test
    @SneakyThrows
    void testConfigCreationActiveRecordExists() {
        final var conciergeContext = new ConciergeContext();
        final var createConfigRequest = ResourceHelper.getResource("configurationRequest.json",
                ConfigurationRequest.class);
        conciergeContext.addContext(ConfigurationRequest.class.getSimpleName(), createConfigRequest);
        ContextUtils.addConfigUpdaterContext(conciergeContext, getConfigUpdater());
        Mockito.when(getConciergeRepository().createdRecordExists(Mockito.any(), Mockito.any()))
                .thenReturn(true);
        final var processor = getConciergeProcessor();
        Assertions.assertThrows(ConciergeException.class, () -> processor.process(conciergeContext));
    }

    @Test
    @SneakyThrows
    void testConfigCreationNoActiveRecord() {
        final var conciergeContext = new ConciergeContext();
        final var createConfigRequest = ResourceHelper.getResource("configurationRequest.json",
                ConfigurationRequest.class);
        conciergeContext.addContext(ConfigurationRequest.class.getSimpleName(), createConfigRequest);
        ContextUtils.addConfigUpdaterContext(conciergeContext, getConfigUpdater());
        Mockito.when(getConciergeRepository().createdRecordExists(Mockito.any(), Mockito.any()))
                .thenReturn(false);
        final var processor = getConciergeProcessor();
        processor.process(conciergeContext);
        Mockito.verify(getConciergeRepository(), Mockito.times(1))
                .create(Mockito.any(ConfigDetails.class));
    }
}
