package com.grookage.concierge.core.utils;

import com.grookage.concierge.core.engine.ConciergeContext;
import com.grookage.concierge.models.ConfigUpdater;
import com.grookage.concierge.models.ResourceHelper;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.config.ConfigHistoryItem;
import com.grookage.concierge.models.exception.ConciergeException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigurationUtilsTest {

    private static final ConfigUpdater testUpdater = new ConfigUpdater() {
        @Override
        public String name() {
            return "n";
        }

        @Override
        public String userId() {
            return "U1";
        }

        @Override
        public String email() {
            return "email";
        }
    };

    @Test
    @SneakyThrows
    void testConfigApprovalInValidUser() {
        final var configDetails = ResourceHelper.getResource(
                "configDetails.json",
                ConfigDetails.class
        );
        final var history = ResourceHelper.getResource(
                "configHistory.json",
                ConfigHistoryItem.class
        );
        configDetails.addHistory(history);
        final var context = new ConciergeContext();
        ContextUtils.addConfigUpdaterContext(context, testUpdater);
        Assertions.assertThrows(ConciergeException.class, () -> ConfigurationUtils.validateConfigApproveAccess(
                configDetails,
                context
        ));
    }

    @Test
    @SneakyThrows
    void testConfigApprovalSameUserIdOtherState() {
        final var configDetails = ResourceHelper.getResource(
                "configDetails.json",
                ConfigDetails.class
        );
        final var history = ResourceHelper.getResource(
                "configHistory.json",
                ConfigHistoryItem.class
        );
        history.setConfigEvent(ConfigEvent.APPROVE_CONFIG);
        configDetails.addHistory(history);
        final var context = new ConciergeContext();
        ContextUtils.addConfigUpdaterContext(context, testUpdater);
        Assertions.assertDoesNotThrow(() -> ConfigurationUtils.validateConfigApproveAccess(
                configDetails,
                context
        ));
    }

    @Test
    @SneakyThrows
    void testConfigApprovalValidUser() {
        final var configDetails = ResourceHelper.getResource(
                "configDetails.json",
                ConfigDetails.class
        );
        final var history = ResourceHelper.getResource(
                "configHistory.json",
                ConfigHistoryItem.class
        );
        history.setConfigUpdaterId("ccc1");
        configDetails.addHistory(history);
        final var context = new ConciergeContext();
        ContextUtils.addConfigUpdaterContext(context, testUpdater);
        Assertions.assertDoesNotThrow(() -> ConfigurationUtils.validateConfigApproveAccess(
                configDetails,
                context
        ));
    }
}
