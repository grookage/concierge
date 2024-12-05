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

package com.grookage.concierge.elastic.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import com.google.common.base.Preconditions;
import com.grookage.concierge.elastic.client.ElasticClientManager;
import com.grookage.concierge.elastic.config.ElasticConfig;
import com.grookage.concierge.elastic.storage.StoredElasticRecord;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.config.ConfigState;
import com.grookage.concierge.repository.AbstractConciergeRepository;
import com.grookage.concierge.repository.cache.CacheConfig;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class ElasticRepository extends AbstractConciergeRepository {

    private static final String CONFIG_INDEX = "config_registry";
    private static final String NAMESPACE = "namespace";
    private static final String CONFIG_NAME = "configName";
    private static final String VERSION = "version";
    private static final String CONFIG_STATE = "configState";
    private final ElasticsearchClient client;
    private final ElasticConfig elasticConfig;

    public ElasticRepository(ElasticConfig elasticConfig, CacheConfig cacheConfig) {
        super(cacheConfig);
        Preconditions.checkNotNull(elasticConfig, "Elastic Config can't be null");
        this.elasticConfig = elasticConfig;
        this.client = new ElasticClientManager(elasticConfig).getElasticClient();
        this.initialize();
    }

    @SneakyThrows
    private void initialize() {
        final var indexExists = client.indices()
                .exists(ExistsRequest.of(s -> s.index(CONFIG_INDEX)))
                .value();
        if (!indexExists) {
            final var registryInitialized = client.indices().create(CreateIndexRequest.of(idx -> idx.index(CONFIG_INDEX)
                    .settings(IndexSettings.of(s -> s.numberOfShards("1")
                            .numberOfReplicas("2"))))
            ).shardsAcknowledged();
            if (!registryInitialized) {
                throw new IllegalStateException("Registry index creation seems to have failed, please try again!");
            }
        }
    }

    /* Fields and values are being lower-cased, before adding as clauses, since elasticsearch deals with lowercase only */
    private List<FieldValue> getNormalizedValues(Set<String> terms) {
        return terms.stream().map(FieldValue::of).toList();
    }

    private StoredElasticRecord toStorageRecord(ConfigDetails configDetails) {
        return StoredElasticRecord.builder()
                .data(configDetails.getData())
                .configHistories(configDetails.getConfigHistories())
                .configState(configDetails.getConfigState())
                .version(configDetails.getConfigKey().getVersion())
                .description(configDetails.getDescription())
                .namespace(configDetails.getConfigKey().getNamespace())
                .configName(configDetails.getConfigKey().getConfigName())
                .build();
    }

    private ConfigDetails toConfigDetails(StoredElasticRecord storedElasticRecord) {
        return ConfigDetails.builder()
                .data(storedElasticRecord.getData())
                .configHistories(storedElasticRecord.getConfigHistories())
                .configState(storedElasticRecord.getConfigState())
                .description(storedElasticRecord.getDescription())
                .configKey(ConfigKey.builder()
                        .version(storedElasticRecord.getVersion())
                        .namespace(storedElasticRecord.getNamespace())
                        .configName(storedElasticRecord.getConfigName())
                        .build())
                .build();
    }

    @SneakyThrows
    private List<ConfigDetails> queryDetails(final Query searchQuery,
                                             final Predicate<Hit<StoredElasticRecord>> searchPredicate) {
        final var searchResponse = client.search(SearchRequest.of(
                        s -> s.query(searchQuery)
                                .requestCache(true)
                                .index(List.of(CONFIG_INDEX))
                                .size(elasticConfig.getMaxResultSize()) //If you have more than 10K schemas, this will hold you up!
                                .timeout(elasticConfig.getTimeout())),
                StoredElasticRecord.class
        );
        return searchResponse.hits()
                .hits()
                .stream()
                .filter(searchPredicate)
                .map(each -> toConfigDetails(Objects.requireNonNull(each.source()))).toList();
    }

    @Override
    @SneakyThrows
    public Optional<ConfigDetails> getStoredRecord(ConfigKey configKey) {
        final var getResponse = client.get(GetRequest.of(request ->
                        request.index(CONFIG_INDEX).id(configKey.getReferenceId())),
                StoredElasticRecord.class);
        return Optional.ofNullable(getResponse.source()).map(this::toConfigDetails);
    }

    @Override
    public List<ConfigDetails> getStoredRecords(String namespace,
                                                Set<String> configNames,
                                                Set<ConfigState> configStates) {
        final var namespaceQuery = TermQuery.of(p -> p.field(NAMESPACE).value(namespace))._toQuery();
        final var configQuery = TermsQuery.of(q -> q.field(CONFIG_NAME)
                .terms(t -> t.value(getNormalizedValues(configNames))))._toQuery();
        final var configStateQuery = TermsQuery.of(q -> q.field(CONFIG_STATE)
                        .terms(t -> t.value(getNormalizedValues(configStates.stream().map(Enum::name).collect(Collectors.toSet()))))).
                _toQuery();
        final var searchQuery = BoolQuery.of(q -> q.must(List.of(namespaceQuery, configQuery, configStateQuery)))._toQuery();
        return queryDetails(searchQuery, storedElasticRecordHit -> true);
    }

    @Override
    public List<ConfigDetails> getActiveStoredRecords(Set<String> namespaces) {
        final var namespaceQuery = namespaces.isEmpty() ?
                MatchAllQuery.of(q -> q)._toQuery() :
                TermsQuery.of(q -> q.field(NAMESPACE)
                        .terms(t -> t.value(getNormalizedValues(namespaces))))._toQuery();
        final var searchQuery = BoolQuery.of(q -> q.must(List.of(namespaceQuery)))._toQuery();
        return queryDetails(searchQuery, storedElasticRecordHit -> storedElasticRecordHit.source() != null &&
                storedElasticRecordHit.source().getConfigState() == ConfigState.ACTIVATED);
    }

    @Override
    @SneakyThrows
    public List<ConfigDetails> getStoredRecords(Set<String> namespaces) {
        if (namespaces.isEmpty()) {
            return getStoredRecords();
        }

        final var namespaceQuery = TermsQuery.of(q -> q.field(NAMESPACE)
                .terms(t -> t.value(getNormalizedValues(namespaces))))._toQuery();
        final var searchQuery = BoolQuery.of(q -> q.must(List.of(namespaceQuery)))._toQuery();
        return queryDetails(searchQuery, storedElasticRecordHit -> true);
    }

    @Override
    @SneakyThrows
    public void save(ConfigDetails configDetails) {
        final var createDocument = new IndexRequest.Builder<>().document(toStorageRecord(configDetails))
                .index(CONFIG_INDEX)
                .refresh(Refresh.WaitFor)
                .id(configDetails.getReferenceId())
                .timeout(Time.of(s -> s.time(elasticConfig.getTimeout())))
                .build();
        client.index(createDocument);
    }

    @Override
    @SneakyThrows
    public void update(ConfigDetails configDetails) {
        final var updateRequest = new UpdateRequest.Builder<>()
                .index(CONFIG_INDEX)
                .id(configDetails.getReferenceId())
                .doc(toStorageRecord(configDetails))
                .refresh(Refresh.WaitFor)
                .timeout(Time.of(s -> s.time(elasticConfig.getTimeout())))
                .build();
        client.update(updateRequest, StoredElasticRecord.class);
    }

    @Override
    @SneakyThrows
    public List<ConfigDetails> getStoredRecords() {
        final var query = MatchAllQuery.of(q -> q)._toQuery();
        final var searchQuery = BoolQuery.of(q -> q.must(List.of(query)))._toQuery();
        return queryDetails(searchQuery, storedElasticRecordHit -> true);
    }
}
