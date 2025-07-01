/*
 * Copyright (c) 2024. Koushik R <rkoushik.14@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.grookage.concierge.dwclient;

import com.google.common.base.Preconditions;
import com.grookage.concierge.client.ConciergeClient;
import com.grookage.concierge.client.ConciergeMeta;
import com.grookage.concierge.client.refresher.ConciergeClientRefresher;
import com.grookage.concierge.client.refresher.ConciergeClientSupplier;
import com.grookage.concierge.client.serde.SerDeFactory;
import com.grookage.concierge.models.ingestion.ConfigurationResponse;
import com.grookage.korg.config.KorgHttpConfiguration;
import com.grookage.korg.consumer.KorgConsumer;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Environment;
import lombok.Getter;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Getter
public abstract class ConciergeClientBundle<T extends Configuration> implements ConfiguredBundle<T> {

    private ConciergeClient conciergeClient;

    protected abstract ConciergeMeta getConciergeMeta(T configuration);

    protected int getRefreshIntervalSeconds(T configuration) {
        return 30;
    }

    protected boolean refreshEnabled(T configuration) {
        return false;
    }

    protected abstract KorgHttpConfiguration getHttpConfiguration(T configuration);

    protected Supplier<String> getAuthHeaderSupplier(T configuration) {
        return () -> null;
    }

    protected abstract SerDeFactory getSerDeFactory(T configuration);

    protected Supplier<KorgConsumer<List<ConfigurationResponse>>> getConfigConsumer(T configuration) {
        return () -> null;
    }

    @Override
    public void run(T configuration, Environment environment) {
        final var meta = getConciergeMeta(configuration);
        Preconditions.checkNotNull(meta, "Concierge Meta can't be null");

        final var httpConfiguration = getHttpConfiguration(configuration);
        Preconditions.checkNotNull(httpConfiguration, "Http Configuration can't be null");

        final var serdeFactory = getSerDeFactory(configuration);
        Preconditions.checkNotNull(serdeFactory, "SerDeFactory can't be null");

        final var dataRefreshSeconds = getRefreshIntervalSeconds(configuration);
        final var configConsumer = getConfigConsumer(configuration);

        final var clientRefresher = ConciergeClientRefresher.builder()
                .supplier(
                        ConciergeClientSupplier.builder()
                                .httpConfiguration(httpConfiguration)
                                .meta(meta)
                                .authHeaderSupplier(getAuthHeaderSupplier(configuration))
                                .build()
                )
                .refreshTimeInSeconds(dataRefreshSeconds)
                .periodicRefresh(refreshEnabled(configuration))
                .configConsumer(configConsumer)
                .build();

        conciergeClient = ConciergeClient.builder()
                .refresher(clientRefresher)
                .serDeFactory(serdeFactory)
                .build();
    }
}
