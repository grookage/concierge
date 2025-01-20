package com.grookage.concierge.models.processor;

import com.grookage.concierge.models.ResourceHelper;
import com.grookage.concierge.models.config.ConfigEvent;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProcessorKeyTest {
    @Test
    @SneakyThrows
    void testProcessorKey() {
        final var processorKey = ResourceHelper
                .getResource("processorKey.json", ProcessorKey.class);
        Assertions.assertNotNull(processorKey);
        Assertions.assertEquals("testNamespace", processorKey.getNamespace());
        Assertions.assertEquals("testConfig", processorKey.getConfigName());
        Assertions.assertEquals(ConfigEvent.CREATE_CONFIG, processorKey.getConfigEvent());
    }
}
