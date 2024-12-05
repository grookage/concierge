package com.grookage.concierge.client;

import com.grookage.concierge.client.refresher.ConciergeClientRefresher;
import com.grookage.concierge.client.serde.SerDeFactory;
import com.grookage.concierge.models.config.ConfigKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Builder
public class ConciergeClient {

    private final SerDeFactory serDeFactory;
    private final ConciergeClientRefresher refresher;

    public <T> T getConfiguration(final ConfigKey configKey) {
        final var configurations = refresher.getData();
        final var serde = serDeFactory.getSerDe(configKey.getConfigName());
        final var responseConfiguration = configurations.stream().filter(each -> each.getConfigKey().equals(configKey))
                .findFirst().orElse(null);
        return null == responseConfiguration ? null : serde.convert(responseConfiguration);
    }

}
