package com.grookage.concierge.core.utils;

import com.grookage.concierge.core.engine.ConciergeContext;
import com.grookage.concierge.models.MapperUtils;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.config.ConfigState;
import com.grookage.concierge.models.exception.ConciergeCoreErrorCode;
import com.grookage.concierge.models.exception.ConciergeException;
import com.grookage.concierge.models.ingestion.ConfigurationRequest;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiPredicate;

@UtilityClass
@Slf4j
public class ConfigurationUtils {

    private static final boolean LOCAL_ENV = Boolean.parseBoolean(System.getProperty("localEnv", "false"));
    //Both config creator and configUpdater can't approve the config.
    private static final BiPredicate<ConfigDetails, ConciergeContext> CONFIG_PREDICATE = (configDetails, context) -> {
        final var requestingUser = ContextUtils.getUserId(context);
        return configDetails.getConfigHistories()
                .stream()
                .filter(each -> each.getConfigEvent() == ConfigEvent.CREATE_CONFIG ||
                        each.getConfigEvent() == ConfigEvent.UPDATE_CONFIG)
                .anyMatch(each -> each.getConfigUpdaterId().equals(requestingUser));
    };

    public static void validateConfigApproveAccess(final ConfigDetails configDetails, final ConciergeContext context) {
        if (!LOCAL_ENV && CONFIG_PREDICATE.test(configDetails, context)) {
            log.error("User {} is not allowed to approve the config {}. The userId is same as the config creator",
                    ContextUtils.getUserId(context),
                    configDetails.getConfigKey());
            throw ConciergeException.error(ConciergeCoreErrorCode.INVALID_USER);
        }
    }

    @SneakyThrows
    public ConfigDetails toConfigDetails(ConfigurationRequest configurationRequest) {
        return ConfigDetails.builder()
                .configKey(configurationRequest.getConfigKey())
                .configState(ConfigState.CREATED)
                .description(configurationRequest.getDescription())
                .data(MapperUtils.mapper().writeValueAsBytes(configurationRequest.getData()))
                .build();
    }
}
