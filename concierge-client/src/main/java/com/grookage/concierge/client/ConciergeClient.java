package com.grookage.concierge.client;

import com.grookage.concierge.client.refresher.ConciergeClientRefresher;
import com.grookage.concierge.client.serde.SerDeFactory;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.ingestion.ConfigurationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Builder
public class ConciergeClient {

    private final SerDeFactory serDeFactory;
    private final ConciergeClientRefresher refresher;

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getConfiguration(final ConfigKey configKey) {
        final var configurations = refresher.getData();
        final var serde = serDeFactory.getSerDe(configKey);
        final var responseConfiguration = getMatchingConfig(configurations, configKey).orElse(null);
        return null == responseConfiguration ? Optional.empty() :
                (Optional<T>) Optional.of(serde.convert(responseConfiguration));
    }

    private Optional<ConfigurationResponse> getMatchingConfig(
            final List<ConfigurationResponse> allConfigs,
            final ConfigKey configKey
    ) {
        return configKey.latest() ?
                allConfigs.stream()
                        .filter(each -> each.getConfigKey().getReferenceTag().equals(configKey.getReferenceTag()))
                        .max(Comparator.naturalOrder()) :
                allConfigs.stream()
                        .filter(each -> each.getConfigKey().getReferenceId().equals(configKey.getReferenceId()))
                        .findFirst();
    }
}
