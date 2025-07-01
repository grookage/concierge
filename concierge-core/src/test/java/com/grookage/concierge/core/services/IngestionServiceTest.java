package com.grookage.concierge.core.services;

import com.grookage.concierge.core.engine.ConciergeContext;
import com.grookage.concierge.core.engine.ConciergeHub;
import com.grookage.concierge.core.engine.ConciergeProcessor;
import com.grookage.concierge.core.managers.ProcessorFactory;
import com.grookage.concierge.core.services.impl.IngestionServiceImpl;
import com.grookage.concierge.core.stubs.TestConfigUpdater;
import com.grookage.concierge.models.ResourceHelper;
import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.exception.ConciergeCoreErrorCode;
import com.grookage.concierge.models.exception.ConciergeException;
import com.grookage.concierge.models.ingestion.ConfigurationRequest;
import com.grookage.concierge.models.ingestion.UpdateConfigRequest;
import com.grookage.concierge.models.processor.ProcessorKey;
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.function.Supplier;

class IngestionServiceTest {

    @Test
    @SneakyThrows
    void testConfigProcessorInvocations() {
        final var processor = Mockito.mock(ConciergeProcessor.class);
        final var conciergeHub = Mockito.mock(ConciergeHub.class);
        Mockito.when(conciergeHub.getProcessor(ConfigEvent.CREATE_CONFIG))
                .thenReturn(Optional.of(processor));
        final var ingestionService = new IngestionServiceImpl<TestConfigUpdater>(conciergeHub);
        final var configurationRequest = ResourceHelper.getResource(
                "configurationRequest.json",
                ConfigurationRequest.class
        );
        final var configUpdater = new TestConfigUpdater();
        ingestionService.createConfiguration(configUpdater, configurationRequest);
        Mockito.verify(processor, Mockito.times(1))
                .fire(Mockito.any());
        final var updateConfigRequest = ResourceHelper.getResource(
                "updateConfiguration.json",
                UpdateConfigRequest.class
        );
        var exception = Assertions.assertThrows(ConciergeException.class, () ->
                ingestionService.updateConfiguration(configUpdater, updateConfigRequest));
        Assertions.assertEquals(exception.getCode(), ConciergeCoreErrorCode.PROCESSOR_NOT_FOUND.name());
        Mockito.when(conciergeHub.getProcessor(ConfigEvent.UPDATE_CONFIG))
                .thenReturn(Optional.of(processor));
        ingestionService.updateConfiguration(configUpdater, updateConfigRequest);
        Mockito.verify(processor, Mockito.times(2))
                .fire(Mockito.any());
        exception = Assertions.assertThrows(ConciergeException.class, () ->
                ingestionService.appendConfiguration(configUpdater, updateConfigRequest));
        Assertions.assertEquals(exception.getCode(), ConciergeCoreErrorCode.PROCESSOR_NOT_FOUND.name());
        Mockito.when(conciergeHub.getProcessor(ConfigEvent.APPEND_CONFIG))
                .thenReturn(Optional.of(processor));
        ingestionService.appendConfiguration(configUpdater, updateConfigRequest);
        Mockito.verify(processor, Mockito.times(3))
                .fire(Mockito.any());
    }

    @Test
    @SneakyThrows
    void testConfigProcessorInvocationsForFinalStates() {
        final var processor = Mockito.mock(ConciergeProcessor.class);
        final var conciergeHub = Mockito.mock(ConciergeHub.class);
        final var ingestionService = new IngestionServiceImpl<TestConfigUpdater>(conciergeHub);
        final var configUpdater = new TestConfigUpdater();
        final var configKey = ResourceHelper.getResource(
                "configKey.json",
                ConfigKey.class
        );
        var exception = Assertions.assertThrows(ConciergeException.class, () ->
                ingestionService.approveConfiguration(configUpdater, configKey));
        Assertions.assertEquals(exception.getCode(), ConciergeCoreErrorCode.PROCESSOR_NOT_FOUND.name());
        exception = Assertions.assertThrows(ConciergeException.class, () ->
                ingestionService.rejectConfiguration(configUpdater, configKey));
        Assertions.assertEquals(exception.getCode(), ConciergeCoreErrorCode.PROCESSOR_NOT_FOUND.name());
        exception = Assertions.assertThrows(ConciergeException.class, () ->
                ingestionService.activateConfiguration(configUpdater, configKey));
        Assertions.assertEquals(exception.getCode(), ConciergeCoreErrorCode.PROCESSOR_NOT_FOUND.name());
        Mockito.when(conciergeHub.getProcessor(ConfigEvent.APPROVE_CONFIG))
                .thenReturn(Optional.of(processor));
        Mockito.when(conciergeHub.getProcessor(ConfigEvent.REJECT_CONFIG))
                .thenReturn(Optional.of(processor));
        Mockito.when(conciergeHub.getProcessor(ConfigEvent.ACTIVATE_CONFIG))
                .thenReturn(Optional.of(processor));
        ingestionService.approveConfiguration(configUpdater, configKey);
        ingestionService.rejectConfiguration(configUpdater, configKey);
        ingestionService.activateConfiguration(configUpdater, configKey);
        Mockito.verify(processor, Mockito.times(3))
                .fire(Mockito.any());
    }

    @Test
    @SneakyThrows
    void testConfigProcessorInvocationsWithoutMock() {
        final var processor = new TestConfigProcessor(null);
        final var conciergeHub = Mockito.mock(ConciergeHub.class);
        Mockito.when(conciergeHub.getProcessor(Mockito.any(ConfigEvent.class)))
                .thenReturn(Optional.of(processor));
        final var ingestionService = new IngestionServiceImpl<TestConfigUpdater>(conciergeHub);
        final var configurationRequest = ResourceHelper.getResource(
                "configurationRequest.json",
                ConfigurationRequest.class
        );
        final var configUpdater = new TestConfigUpdater();
        ingestionService.createConfiguration(configUpdater, configurationRequest);
        Assertions.assertNotNull(processor.getProcessorKey());
        Assertions.assertEquals(ConfigEvent.CREATE_CONFIG, processor.getProcessorKey().getConfigEvent());
        final var updateConfigRequest = ResourceHelper.getResource(
                "updateConfiguration.json",
                UpdateConfigRequest.class
        );
        ingestionService.updateConfiguration(configUpdater, updateConfigRequest);
        Assertions.assertNotNull(processor.getProcessorKey());
        Assertions.assertEquals(ConfigEvent.UPDATE_CONFIG, processor.getProcessorKey().getConfigEvent());
        ingestionService.appendConfiguration(configUpdater, updateConfigRequest);
        Assertions.assertNotNull(processor.getProcessorKey());
        Assertions.assertEquals(ConfigEvent.APPEND_CONFIG, processor.getProcessorKey().getConfigEvent());
        final var configKey = ResourceHelper.getResource(
                "configKey.json",
                ConfigKey.class
        );
        ingestionService.approveConfiguration(configUpdater, configKey);
        Assertions.assertNotNull(processor.getProcessorKey());
        Assertions.assertEquals(ConfigEvent.APPROVE_CONFIG, processor.getProcessorKey().getConfigEvent());
        ingestionService.rejectConfiguration(configUpdater, configKey);
        Assertions.assertNotNull(processor.getProcessorKey());
        Assertions.assertEquals(ConfigEvent.REJECT_CONFIG, processor.getProcessorKey().getConfigEvent());
        ingestionService.activateConfiguration(configUpdater, configKey);
        Assertions.assertNotNull(processor.getProcessorKey());
        Assertions.assertEquals(ConfigEvent.ACTIVATE_CONFIG, processor.getProcessorKey().getConfigEvent());
    }

    @Getter
    static class TestConfigProcessor extends ConciergeProcessor {

        private ProcessorKey processorKey;

        public TestConfigProcessor(Supplier<ProcessorFactory> processorFactory) {
            super(processorFactory);
        }

        @Override
        public ConfigEvent name() {
            return null;
        }

        @Override
        @SneakyThrows
        public void process(ConciergeContext context) {
            processorKey = context.getContext(ProcessorKey.class).orElse(null);
        }
    }

}
