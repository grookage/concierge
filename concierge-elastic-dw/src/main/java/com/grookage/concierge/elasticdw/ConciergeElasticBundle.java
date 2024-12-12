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

package com.grookage.concierge.elasticdw;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.grookage.conceirge.dwserver.ConciergeBundle;
import com.grookage.conceirge.dwserver.health.ConciergeHealthCheck;
import com.grookage.concierge.elastic.config.ElasticConfig;
import com.grookage.concierge.elastic.repository.ElasticRepository;
import com.grookage.concierge.models.ConfigUpdater;
import com.grookage.concierge.repository.ConciergeRepository;
import com.grookage.concierge.repository.cache.CacheConfig;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Supplier;

@NoArgsConstructor
@Getter
public abstract class ConciergeElasticBundle<T extends Configuration, U extends ConfigUpdater> extends ConciergeBundle<T, U> {

    private ElasticConfig elasticConfig;
    private ElasticsearchClient elasticsearchClient;
    private ElasticRepository elasticRepository;

    protected abstract ElasticConfig getElasticConfig(T configuration);


    protected abstract CacheConfig getCacheConfig(T configuration);

    @Override
    protected Supplier<ConciergeRepository> getRepositorySupplier(T configuration) {
        return () -> elasticRepository;
    }

    @Override
    protected List<ConciergeHealthCheck> withHealthChecks(T configuration) {
        return List.of(new ElasticHealthCheck(elasticConfig, elasticsearchClient));
    }

    @Override
    public void run(T configuration, Environment environment) {
        this.elasticConfig = getElasticConfig(configuration);
        final var cacheConfig = getCacheConfig(configuration);
        this.elasticRepository = new ElasticRepository(elasticConfig, cacheConfig);
        this.elasticsearchClient = elasticRepository.getClient();
        super.run(configuration, environment);
    }
}


