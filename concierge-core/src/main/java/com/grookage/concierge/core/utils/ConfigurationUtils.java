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

    public static final BiPredicate<ConfigDetails, ConciergeContext> CONFIG_MAINTAINER_PREDICATE = (configDetails, context) -> {
        final var requestingUser = ContextUtils.getUserId(context);
        final var configCreatorId = ConfigurationUtils.getConfigCreatorId(configDetails);
        return !requestingUser.equals(configCreatorId);
    };

    public static final BiPredicate<ConfigDetails, ConciergeContext> CONFIG_UPDATER_PREDICATE = (configDetails, context) -> {
        final var requestingUser = ContextUtils.getUserId(context);
        final var configCreatorId = ConfigurationUtils.getConfigCreatorId(configDetails);
        return requestingUser.equals(configCreatorId);
    };


    @SneakyThrows
    public ConfigDetails toConfigDetails(ConfigurationRequest configurationRequest) {
        return ConfigDetails.builder()
                .configKey(configurationRequest.getConfigKey())
                .configState(ConfigState.CREATED)
                .description(configurationRequest.getDescription())
                .data(MapperUtils.mapper().writeValueAsBytes(configurationRequest.getData()))
                .build();
    }

    public String getConfigCreatorId(final ConfigDetails configDetails) {
        final var createConfigHistory = configDetails.getConfigHistories()
                .stream()
                .filter(each -> each.getConfigEvent() == ConfigEvent.CREATE_CONFIG)
                .findFirst().orElse(null);
        if (null == createConfigHistory) {
            log.error("There is no history present for createConfig event for configKey {}. Please try creating the config first",
                    configDetails.getConfigKey());
            throw ConciergeException.error(ConciergeCoreErrorCode.NO_CONFIG_FOUND);
        }
        return createConfigHistory.getConfigUpdaterId();
    }
}
