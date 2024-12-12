package com.grookage.concierge.models.ingestion;

import com.grookage.concierge.models.ResourceHelper;
import com.grookage.concierge.models.config.ConfigState;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class IngestionRequestTest {

    @Test
    @SneakyThrows
    void testConfigurationRequest() {
        final var configurationRequest = ResourceHelper.getResource("configurationRequest.json",
                ConfigurationRequest.class);
        Assertions.assertNotNull(configurationRequest);
        Assertions.assertEquals("concierge", configurationRequest.getNamespace());
        Assertions.assertEquals("testConfig", configurationRequest.getConfigName());
        Assertions.assertNotNull(configurationRequest.getData());
    }

    @Test
    @SneakyThrows
    void testUpdateConfigurationRequest() {
        final var updateConfigurationRequest = ResourceHelper.getResource("updateConfiguration.json",
                UpdateConfigRequest.class);
        Assertions.assertNotNull(updateConfigurationRequest);
        Assertions.assertEquals("concierge", updateConfigurationRequest.getNamespace());
        Assertions.assertEquals("testConfig", updateConfigurationRequest.getConfigName());
        Assertions.assertEquals("V12345", updateConfigurationRequest.getVersion());
        Assertions.assertNotNull(updateConfigurationRequest.getData());
    }

    @Test
    @SneakyThrows
    void testConfigurationResponse() {
        final var configurationResponse = ResourceHelper.getResource("configurationResponse.json",
                ConfigurationResponse.class);
        Assertions.assertNotNull(configurationResponse);
        Assertions.assertNotNull(configurationResponse.getConfigKey());
        final var configKey = configurationResponse.getConfigKey();
        Assertions.assertEquals("concierge", configKey.getNamespace());
        Assertions.assertEquals("testConfig", configKey.getConfigName());
        Assertions.assertEquals("V12345", configKey.getVersion());
        Assertions.assertTrue(configurationResponse.getConfigHistories().isEmpty());
        Assertions.assertEquals(ConfigState.CREATED, configurationResponse.getConfigState());
    }
}
