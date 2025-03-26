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
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.config.ConfigState;
import com.grookage.concierge.repository.ConciergeRepository;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class ElasticRepository implements ConciergeRepository {

    private static final String CONFIG_INDEX = "config_registry";
    private static final String ORG = "configKey.orgId";
    private static final String NAMESPACE = "configKey.namespace";
    private static final String TENANT = "configKey.tenantId";
    private static final String CONFIG_NAME = "configKey.configName";
    private static final String VERSION = "configKey.version";
    private static final String CONFIG_STATE = "configKey.configState";
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

    /* Fields and values are being lower-cased, before adding as clauses, since elasticsearch deals with lowercase only */
    private List<FieldValue> getNormalizedValues(Collection<String> terms) {
        return terms.stream().map(FieldValue::of).toList();
    }

    @SneakyThrows
    private List<ConfigDetails> queryDetails(final Query searchQuery,
                                             final Predicate<Hit<ConfigDetails>> searchPredicate) {
        final var searchResponse = client.search(SearchRequest.of(
                        s -> s.query(searchQuery)
                                .requestCache(true)
                                .index(List.of(CONFIG_INDEX))
                                .size(elasticConfig.getMaxResultSize()) //If you have more than 10K schemas, this will hold you up!
                                .timeout(elasticConfig.getTimeout())),
                ConfigDetails.class
        );
        return searchResponse.hits()
                .hits()
                .stream()
                .filter(searchPredicate)
                .map(each -> Objects.requireNonNull(each.source())).toList();
    }

    @Override
    @SneakyThrows
    public boolean createdRecordExists(ConfigKey configKey) {
        final var orgQuery = TermQuery.of(p -> p.field(ORG).value(configKey.getOrgId()))._toQuery();
        final var namespaceQuery = TermQuery.of(p -> p.field(NAMESPACE).value(configKey.getNamespace()))._toQuery();
        final var tenantQuery = TermQuery.of(p -> p.field(TENANT).value(configKey.getTenantId()))._toQuery();
        final var configQuery = TermQuery.of(p -> p.field(CONFIG_NAME).value(configKey.getConfigName()))._toQuery();
        final var configStateQuery = TermQuery.of(p -> p.field(CONFIG_STATE).value(ConfigState.CREATED.name()))._toQuery();
        final var searchQuery = BoolQuery.of(q -> q.must(List.of(orgQuery, namespaceQuery, tenantQuery, configQuery, configStateQuery)))._toQuery();
        final var searchResponse = client.search(SearchRequest.of(
                        s -> s.query(searchQuery)
                                .requestCache(true)
                                .index(List.of(CONFIG_INDEX))
                                .size(elasticConfig.getMaxResultSize()) //If you have more than 10K schemas, this will hold you up!
                                .timeout(elasticConfig.getTimeout())),
                ConfigDetails.class
        );
        return !searchResponse.hits().hits().isEmpty();
    }

    @Override
    public List<ConfigDetails> getStoredRecords(com.grookage.concierge.models.SearchRequest searchRequest) {

        final var namespaceQuery = searchRequest.getNamespaces().isEmpty() ? MatchAllQuery.of(q -> q)._toQuery() :
                TermsQuery.of(q -> q.field(NAMESPACE).terms(t -> t.value(getNormalizedValues(searchRequest.getNamespaces()))))._toQuery();
        final var orgQuery = searchRequest.getOrgs().isEmpty() ? MatchAllQuery.of(q -> q)._toQuery() :
                TermsQuery.of(q -> q.field(ORG).terms(t -> t.value(getNormalizedValues(searchRequest.getOrgs()))))._toQuery();
        final var tenantQuery = searchRequest.getTenants().isEmpty() ? MatchAllQuery.of(q -> q)._toQuery() :
                TermsQuery.of(q -> q.field(TENANT).terms(t -> t.value(getNormalizedValues(searchRequest.getTenants()))))._toQuery();
        final var configQuery = searchRequest.getConfigNames().isEmpty() ? MatchAllQuery.of(q -> q)._toQuery() :
                TermsQuery.of(q -> q.field(CONFIG_NAME).terms(t -> t.value(getNormalizedValues(searchRequest.getConfigNames()))))._toQuery();
        final var configStateQuery = searchRequest.getConfigStates().isEmpty() ? MatchAllQuery.of(q -> q)._toQuery() :
                TermsQuery.of(q -> q.field(CONFIG_STATE).terms(t -> t.value(getNormalizedValues(searchRequest.getConfigStates().stream().map(Enum::name).collect(Collectors.toSet()))))).
                        _toQuery();
        final var searchQuery = BoolQuery.of(q -> q.must(List.of(orgQuery, namespaceQuery, tenantQuery,
                configQuery, configStateQuery)))._toQuery();
        return queryDetails(searchQuery, configDetailsHit -> true);
    }

    @Override
    @SneakyThrows
    public void create(ConfigDetails configDetails) {
        final var createDocument = new IndexRequest.Builder<>().document(configDetails)
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
                .doc(configDetails)
                .refresh(Refresh.WaitFor)
                .timeout(Time.of(s -> s.time(elasticConfig.getTimeout())))
                .build();
        client.update(updateRequest, ConfigDetails.class);
    }

    @Override
    @SneakyThrows
    public Optional<ConfigDetails> getStoredRecord(String referenceId) {
        final var getResponse = client.get(GetRequest.of(request ->
                        request.index(CONFIG_INDEX).id(referenceId)),
                ConfigDetails.class);
        return Optional.ofNullable(getResponse.source());
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
        final var namespaceQuery = TermQuery.of(p -> p.field(NAMESPACE).value(configDetails.getConfigKey().getNamespace()))._toQuery();
        final var configQuery = TermQuery.of(p -> p.field(CONFIG_NAME).value(configDetails.getConfigKey().getConfigName()))._toQuery();
        final var stateQuery = TermQuery.of(p -> p.field(CONFIG_STATE).value(ConfigState.ACTIVATED.name()))._toQuery();
        final var orgQuery = TermQuery.of(p -> p.field(ORG).value(configDetails.getConfigKey().getOrgId()))._toQuery();
        final var tenantQuery = TermQuery.of(p -> p.field(TENANT).value(configDetails.getConfigKey().getTenantId()))._toQuery();
        final var searchQuery = BoolQuery.of(q -> q.must(List.of(orgQuery, namespaceQuery, tenantQuery,
                configQuery, stateQuery)))._toQuery();
        final var searchResponse = client.search(SearchRequest.of(
                        s -> s.query(searchQuery)
                                .requestCache(true)
                                .index(List.of(CONFIG_INDEX))
                                .size(elasticConfig.getMaxResultSize()) //If you have more than 10K schemas, this will hold you up!
                                .timeout(elasticConfig.getTimeout())),
                ConfigDetails.class
        );
        final var newRecords = searchResponse.hits().hits().stream()
                .map(Hit::source).filter(Objects::nonNull)
                .peek(rec -> rec.setConfigState(ConfigState.ROLLED))
                .collect(Collectors.toList());
        newRecords.add(configDetails);
        final var br = new BulkRequest.Builder()
                .index(CONFIG_INDEX)
                .refresh(Refresh.WaitFor)
                .timeout(Time.of(s -> s.time(elasticConfig.getTimeout())));
        newRecords.forEach(eachSchema -> br.operations(op ->
                op.update(idx -> idx.index(CONFIG_INDEX).id(eachSchema.getConfigKey().getReferenceId()).action(a -> a.doc(eachSchema)))));
        client.bulk(br.build());
    }
}
