package com.grookage.concierge.core.services.impl;

import com.grookage.concierge.core.engine.ConciergeContext;
import com.grookage.concierge.core.engine.ConciergeHub;
import com.grookage.concierge.core.engine.ConciergeProcessor;
import com.grookage.concierge.core.stubs.TestConfigUpdater;
import com.grookage.concierge.models.ConfigUpdater;
import com.grookage.concierge.models.ResourceHelper;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.ingestion.ConfigurationRequest;
import com.grookage.concierge.models.ingestion.UpdateConfigRequest;
import com.grookage.concierge.models.processor.ProcessorKey;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;


class IngestionServiceImplTest {
    public static final ConfigUpdater configUpdater = new TestConfigUpdater();
    private IngestionServiceImpl<ConfigUpdater> ingestionService;
    @Mock
    private ConciergeProcessor conciergeProcessor;
    @Mock
    private ConciergeHub conciergeHub;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Mockito.doReturn(Optional.of(conciergeProcessor)).when(conciergeHub).getProcessor(ArgumentMatchers.any(ConfigEvent.class));
        ingestionService = new IngestionServiceImpl<>(conciergeHub);
    }

    @SneakyThrows
    @Test
    void testCreateConfiguration() {

        final var configKey = ResourceHelper.getResource("configKey.json",
                ConfigKey.class);
        final var request = ConfigurationRequest.builder()
                .configKey(configKey)
                .build();
        final var configDetails = ResourceHelper.getResource("configDetails.json", ConfigDetails.class);
        Mockito.doAnswer(invocation -> {
            ConciergeContext context = invocation.getArgument(0);
            context.addContext(ConfigDetails.class.getSimpleName(), configDetails);
            return null;
        }).when(conciergeProcessor).fire(ArgumentMatchers.any(ConciergeContext.class));
        final var result = ingestionService.createConfiguration(configUpdater, request);

        ArgumentCaptor<ConciergeContext> contextCaptor = ArgumentCaptor.forClass(ConciergeContext.class);
        Mockito.verify(conciergeProcessor).fire(contextCaptor.capture());

        ConciergeContext capturedContext = contextCaptor.getValue();
        final var capturedConfigDetails = capturedContext.getContext(ConfigDetails.class);
        final var capturedRequest = capturedContext.getContext(ConfigurationRequest.class);
        final var capturedProcessorKey = capturedContext.getContext(ProcessorKey.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result, capturedConfigDetails.get());
        Assertions.assertEquals(request, capturedRequest.get());
        Assertions.assertEquals(ConfigEvent.CREATE_CONFIG, capturedProcessorKey.get().getConfigEvent());
        Assertions.assertEquals(configKey, capturedProcessorKey.get().getConfigKey());
    }

    @SneakyThrows
    @Test
    void testAppendConfiguration() {
        final var configKey = ResourceHelper.getResource("configKey.json", ConfigKey.class);
        final var request = UpdateConfigRequest.builder()
                .configKey(configKey)
                .build();
        final var configDetails = ResourceHelper.getResource("configDetails.json", ConfigDetails.class);

        Mockito.doAnswer(invocation -> {
            ConciergeContext context = invocation.getArgument(0);
            context.addContext(ConfigDetails.class.getSimpleName(), configDetails);
            return null;
        }).when(conciergeProcessor).fire(ArgumentMatchers.any(ConciergeContext.class));

        final var result = ingestionService.appendConfiguration(configUpdater, request);

        ArgumentCaptor<ConciergeContext> contextCaptor = ArgumentCaptor.forClass(ConciergeContext.class);
        Mockito.verify(conciergeProcessor).fire(contextCaptor.capture());

        ConciergeContext capturedContext = contextCaptor.getValue();
        final var capturedConfigDetails = capturedContext.getContext(ConfigDetails.class);
        final var capturedRequest = capturedContext.getContext(UpdateConfigRequest.class);
        final var capturedProcessorKey = capturedContext.getContext(ProcessorKey.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result, capturedConfigDetails.get());
        Assertions.assertEquals(request, capturedRequest.get());
        Assertions.assertEquals(ConfigEvent.APPEND_CONFIG, capturedProcessorKey.get().getConfigEvent());
        Assertions.assertEquals(configKey, capturedProcessorKey.get().getConfigKey());
    }

    @SneakyThrows
    @Test
    void testUpdateConfiguration() {
        final var configKey = ResourceHelper.getResource("configKey.json", ConfigKey.class);
        final var request = UpdateConfigRequest.builder()
                .configKey(configKey)
                .build();
        final var configDetails = ResourceHelper.getResource("configDetails.json", ConfigDetails.class);

        Mockito.doAnswer(invocation -> {
            ConciergeContext context = invocation.getArgument(0);
            context.addContext(ConfigDetails.class.getSimpleName(), configDetails);
            return null;
        }).when(conciergeProcessor).fire(ArgumentMatchers.any(ConciergeContext.class));

        final var result = ingestionService.updateConfiguration(configUpdater, request);

        ArgumentCaptor<ConciergeContext> contextCaptor = ArgumentCaptor.forClass(ConciergeContext.class);
        Mockito.verify(conciergeProcessor).fire(contextCaptor.capture());

        ConciergeContext capturedContext = contextCaptor.getValue();
        final var capturedConfigDetails = capturedContext.getContext(ConfigDetails.class);
        final var capturedRequest = capturedContext.getContext(UpdateConfigRequest.class);
        final var capturedProcessorKey = capturedContext.getContext(ProcessorKey.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result, capturedConfigDetails.get());
        Assertions.assertEquals(request, capturedRequest.get());
        Assertions.assertEquals(ConfigEvent.UPDATE_CONFIG, capturedProcessorKey.get().getConfigEvent());
        Assertions.assertEquals(configKey, capturedProcessorKey.get().getConfigKey());
    }

    @SneakyThrows
    @Test
    void testApproveConfiguration() {
        final var configKey = ResourceHelper.getResource("configKey.json", ConfigKey.class);
        final var configDetails = ResourceHelper.getResource("configDetails.json", ConfigDetails.class);

        Mockito.doAnswer(invocation -> {
            ConciergeContext context = invocation.getArgument(0);
            context.addContext(ConfigDetails.class.getSimpleName(), configDetails);
            return null;
        }).when(conciergeProcessor).fire(ArgumentMatchers.any(ConciergeContext.class));

        final var result = ingestionService.approveConfiguration(configUpdater, configKey);

        ArgumentCaptor<ConciergeContext> contextCaptor = ArgumentCaptor.forClass(ConciergeContext.class);
        Mockito.verify(conciergeProcessor).fire(contextCaptor.capture());

        ConciergeContext capturedContext = contextCaptor.getValue();
        final var capturedConfigDetails = capturedContext.getContext(ConfigDetails.class);
        final var capturedConfigKey = capturedContext.getContext(ConfigKey.class);
        final var capturedProcessorKey = capturedContext.getContext(ProcessorKey.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result, capturedConfigDetails.get());
        Assertions.assertEquals(configKey, capturedConfigKey.get());
        Assertions.assertEquals(ConfigEvent.APPROVE_CONFIG, capturedProcessorKey.get().getConfigEvent());
        Assertions.assertEquals(configKey, capturedProcessorKey.get().getConfigKey());
    }

    @SneakyThrows
    @Test
    void testRejectConfiguration() {
        final var configKey = ResourceHelper.getResource("configKey.json", ConfigKey.class);
        final var configDetails = ResourceHelper.getResource("configDetails.json", ConfigDetails.class);

        Mockito.doAnswer(invocation -> {
            ConciergeContext context = invocation.getArgument(0);
            context.addContext(ConfigDetails.class.getSimpleName(), configDetails);
            return null;
        }).when(conciergeProcessor).fire(ArgumentMatchers.any(ConciergeContext.class));

        final var result = ingestionService.rejectConfiguration(configUpdater, configKey);

        ArgumentCaptor<ConciergeContext> contextCaptor = ArgumentCaptor.forClass(ConciergeContext.class);
        Mockito.verify(conciergeProcessor).fire(contextCaptor.capture());

        ConciergeContext capturedContext = contextCaptor.getValue();
        final var capturedConfigDetails = capturedContext.getContext(ConfigDetails.class);
        final var capturedConfigKey = capturedContext.getContext(ConfigKey.class);
        final var capturedProcessorKey = capturedContext.getContext(ProcessorKey.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result, capturedConfigDetails.get());
        Assertions.assertEquals(configKey, capturedConfigKey.get());
        Assertions.assertEquals(ConfigEvent.REJECT_CONFIG, capturedProcessorKey.get().getConfigEvent());
        Assertions.assertEquals(configKey, capturedProcessorKey.get().getConfigKey());
    }

    @SneakyThrows
    @Test
    void testActivateConfiguration() {
        final var configKey = ResourceHelper.getResource("configKey.json", ConfigKey.class);
        final var configDetails = ResourceHelper.getResource("configDetails.json", ConfigDetails.class);

        Mockito.doAnswer(invocation -> {
            ConciergeContext context = invocation.getArgument(0);
            context.addContext(ConfigDetails.class.getSimpleName(), configDetails);
            return null;
        }).when(conciergeProcessor).fire(ArgumentMatchers.any(ConciergeContext.class));

        final var result = ingestionService.activateConfiguration(configUpdater, configKey);

        ArgumentCaptor<ConciergeContext> contextCaptor = ArgumentCaptor.forClass(ConciergeContext.class);
        Mockito.verify(conciergeProcessor).fire(contextCaptor.capture());

        ConciergeContext capturedContext = contextCaptor.getValue();
        final var capturedConfigDetails = capturedContext.getContext(ConfigDetails.class);
        final var capturedConfigKey = capturedContext.getContext(ConfigKey.class);
        final var capturedProcessorKey = capturedContext.getContext(ProcessorKey.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result, capturedConfigDetails.get());
        Assertions.assertEquals(configKey, capturedConfigKey.get());
        Assertions.assertEquals(ConfigEvent.ACTIVATE_CONFIG, capturedProcessorKey.get().getConfigEvent());
        Assertions.assertEquals(configKey, capturedProcessorKey.get().getConfigKey());
    }
}
