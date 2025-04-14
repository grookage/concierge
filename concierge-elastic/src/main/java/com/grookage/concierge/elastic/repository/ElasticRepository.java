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
import co.elastic.clients.elasticsearch.core.*;
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
import com.grookage.concierge.repository.ConciergeRepository;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class ElasticRepository implements ConciergeRepository {

    private static final String CONFIG_INDEX = "configs";
    private static final String ORG = "orgId";
    private static final String NAMESPACE = "namespace";
    private static final String TENANT = "tenantId";
    private static final String CONFIG_NAME = "configName";
    private static final String VERSION = "version";
    private static final String CONFIG_STATE = "configState";
    private static final String CONFIG_TYPE = "configType";
    private final ElasticsearchClient client;
    private final ElasticConfig elasticConfig;

    public ElasticRepository(ElasticConfig elasticConfig) {
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


    private FieldValue getNormalizedValue(final String term) {
        return FieldValue.of(term.toLowerCase(Locale.ROOT));
    }

    /* Fields and values are being lower-cased, before adding as clauses, since elasticsearch deals with lowercase only */
    private List<FieldValue> getNormalizedValues(Collection<String> terms) {
        return terms.stream().map(this::getNormalizedValue).toList();
    }

    private StoredElasticRecord toStorageRecord(ConfigDetails configDetails) {
        final var configKey = configDetails.getConfigKey();
        return StoredElasticRecord.builder()
                .data(configDetails.getData())
                .configHistories(configDetails.getConfigHistories())
                .configState(configDetails.getConfigState())
                .description(configDetails.getDescription())
                .orgId(configKey.getOrgId())
                .namespace(configKey.getNamespace())
                .version(configKey.getVersion())
                .configType(configKey.getConfigType())
                .tenantId(configKey.getTenantId())
                .configName(configKey.getConfigName())
                .build();
    }

    private ConfigDetails toConfigDetails(StoredElasticRecord storedElasticRecord) {
        return ConfigDetails.builder()
                .data(storedElasticRecord.getData())
                .configHistories(storedElasticRecord.getConfigHistories())
                .configState(storedElasticRecord.getConfigState())
                .description(storedElasticRecord.getDescription())
                .configKey(storedElasticRecord.getConfigKey())
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
    public boolean createdRecordExists(ConfigKey configKey) {
        final var orgQuery = TermQuery.of(p -> p.field(ORG).value(getNormalizedValue(configKey.getOrgId())))._toQuery();
        final var namespaceQuery = TermQuery.of(p -> p.field(NAMESPACE).value(getNormalizedValue(configKey.getNamespace())))._toQuery();
        final var tenantQuery = TermQuery.of(p -> p.field(TENANT).value(getNormalizedValue(configKey.getTenantId())))._toQuery();
        final var configQuery = TermQuery.of(p -> p.field(CONFIG_NAME).value(getNormalizedValue(configKey.getConfigName())))._toQuery();
        final var configStateQuery = TermQuery.of(p -> p.field(CONFIG_STATE).value(getNormalizedValue(ConfigState.CREATED.name())))._toQuery();
        final var searchQuery = BoolQuery.of(q -> q.must(List.of(orgQuery, namespaceQuery, tenantQuery, configQuery, configStateQuery)))._toQuery();
        final var searchResponse = client.search(SearchRequest.of(
                        s -> s.query(searchQuery)
                                .requestCache(true)
                                .index(List.of(CONFIG_INDEX))
                                .size(elasticConfig.getMaxResultSize()) //If you have more than 10K schemas, this will hold you up!
                                .timeout(elasticConfig.getTimeout())),
                StoredElasticRecord.class
        );
        return !searchResponse.hits().hits().isEmpty();
    }

    @Override
    public List<ConfigDetails> getStoredRecords(com.grookage.concierge.models.SearchRequest searchRequest) {
        final var orgQuery = searchRequest.getOrgs().isEmpty() ? MatchAllQuery.of(q -> q)._toQuery() :
                TermsQuery.of(q -> q.field(ORG).terms(t -> t.value(getNormalizedValues(searchRequest.getOrgs()))))._toQuery();
        final var namespaceQuery = searchRequest.getNamespaces().isEmpty() ? MatchAllQuery.of(q -> q)._toQuery() :
                TermsQuery.of(q -> q.field(NAMESPACE).terms(t -> t.value(getNormalizedValues(searchRequest.getNamespaces()))))._toQuery();
        final var tenantQuery = searchRequest.getTenants().isEmpty() ? MatchAllQuery.of(q -> q)._toQuery() :
                TermsQuery.of(q -> q.field(TENANT).terms(t -> t.value(getNormalizedValues(searchRequest.getTenants()))))._toQuery();
        final var configQuery = searchRequest.getConfigNames().isEmpty() ? MatchAllQuery.of(q -> q)._toQuery() :
                TermsQuery.of(q -> q.field(CONFIG_NAME).terms(t -> t.value(getNormalizedValues(searchRequest.getConfigNames()))))._toQuery();
        final var configTypeQuery = searchRequest.getConfigNames().isEmpty() ? MatchAllQuery.of(q -> q)._toQuery() :
                TermsQuery.of(q -> q.field(CONFIG_TYPE).terms(t -> t.value(getNormalizedValues(searchRequest.getConfigTypes()))))._toQuery();
        final var configStateQuery = searchRequest.getConfigStates().isEmpty() ? MatchAllQuery.of(q -> q)._toQuery() :
                TermsQuery.of(q -> q.field(CONFIG_STATE).terms(t -> t.value(getNormalizedValues(searchRequest.getConfigStates().stream().map(Enum::name).collect(Collectors.toSet()))))).
                        _toQuery();
        final var searchQuery = BoolQuery.of(q -> q.must(List.of(orgQuery, namespaceQuery, tenantQuery,
                configQuery, configTypeQuery, configStateQuery)))._toQuery();
        return queryDetails(searchQuery, storedElasticRecordHit -> true);
    }

    @Override
    @SneakyThrows
    public void create(ConfigDetails configDetails) {
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
    public Optional<ConfigDetails> getStoredRecord(String referenceId) {
        final var getResponse = client.get(GetRequest.of(request ->
                        request.index(CONFIG_INDEX).id(referenceId)),
                StoredElasticRecord.class);
        return Optional.ofNullable(getResponse.source()).map(this::toConfigDetails);
    }

    @Override
    @SneakyThrows
    public List<ConfigDetails> getStoredRecords() {
        final var query = MatchAllQuery.of(q -> q)._toQuery();
        final var searchQuery = BoolQuery.of(q -> q.must(List.of(query)))._toQuery();
        return queryDetails(searchQuery, storedElasticRecordHit -> true);
    }

    @Override
    @SneakyThrows
    public void rollOverAndUpdate(ConfigDetails configDetails) {
        final var namespaceQuery = TermQuery.of(p -> p.field(NAMESPACE).value(getNormalizedValue(configDetails.getConfigKey().getNamespace())))._toQuery();
        final var configQuery = TermQuery.of(p -> p.field(CONFIG_NAME).value(getNormalizedValue(configDetails.getConfigKey().getConfigName())))._toQuery();
        final var stateQuery = TermQuery.of(p -> p.field(CONFIG_STATE).value(getNormalizedValue(ConfigState.ACTIVATED.name())))._toQuery();
        final var orgQuery = TermQuery.of(p -> p.field(ORG).value(getNormalizedValue(configDetails.getConfigKey().getOrgId())))._toQuery();
        final var tenantQuery = TermQuery.of(p -> p.field(TENANT).value(getNormalizedValue(configDetails.getConfigKey().getTenantId())))._toQuery();
        final var searchQuery = BoolQuery.of(q -> q.must(List.of(orgQuery, namespaceQuery, tenantQuery,
                configQuery, stateQuery)))._toQuery();
        final var searchResponse = client.search(SearchRequest.of(
                        s -> s.query(searchQuery)
                                .requestCache(true)
                                .index(List.of(CONFIG_INDEX))
                                .size(elasticConfig.getMaxResultSize()) //If you have more than 10K schemas, this will hold you up!
                                .timeout(elasticConfig.getTimeout())),
                StoredElasticRecord.class
        );
        final var newRecords = searchResponse.hits().hits().stream()
                .map(Hit::source).filter(Objects::nonNull)
                .peek(rec -> rec.setConfigState(ConfigState.ROLLED))
                .collect(Collectors.toList());
        newRecords.add(toStorageRecord(configDetails));
        final var br = new BulkRequest.Builder()
                .index(CONFIG_INDEX)
                .refresh(Refresh.WaitFor)
                .timeout(Time.of(s -> s.time(elasticConfig.getTimeout())));
        newRecords.forEach(eachSchema -> br.operations(op ->
                op.update(idx -> idx.index(CONFIG_INDEX).id(eachSchema.getConfigKey().getReferenceId()).action(a -> a.doc(eachSchema)))));
        client.bulk(br.build());
    }
}