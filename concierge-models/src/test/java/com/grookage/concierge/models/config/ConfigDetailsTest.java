package com.grookage.concierge.models.config;

import com.grookage.concierge.models.ResourceHelper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigDetailsTest {

    @Test
    @SneakyThrows
    void testConfigDetails() {
        final var configDetails = ResourceHelper.getResource("configDetails.json", ConfigDetails.class);
        Assertions.assertNotNull(configDetails);
        Assertions.assertNotNull(configDetails.getConfigKey());
        Assertions.assertEquals("concierge", configDetails.getConfigKey().getNamespace());
        Assertions.assertEquals("testConfig", configDetails.getConfigKey().getConfigName());
        Assertions.assertEquals("V12345", configDetails.getConfigKey().getVersion());
        Assertions.assertEquals(ConfigState.CREATED, configDetails.getConfigState());
        Assertions.assertNotNull(configDetails.getData());
    }

    @Test
    @SneakyThrows
    void testConfigHistory() {
        final var configHistory = ResourceHelper.getResource("configHistory.json",
                ConfigHistoryItem.class);
        Assertions.assertNotNull(configHistory);
        Assertions.assertEquals(ConfigEvent.CREATE_CONFIG, configHistory.getConfigEvent());
        Assertions.assertEquals("conciergeUser", configHistory.getConfigUpdaterName());
    }

}
