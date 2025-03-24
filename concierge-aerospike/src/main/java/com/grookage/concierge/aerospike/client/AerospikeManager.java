package com.grookage.concierge.aerospike.client;

import com.aerospike.client.Record;
import com.aerospike.client.*;
import com.aerospike.client.exp.Exp;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.Statement;
import com.grookage.concierge.aerospike.storage.AerospikeRecord;
import com.grookage.concierge.aerospike.storage.AerospikeStorageConstants;
import com.grookage.concierge.models.MapperUtils;
import com.grookage.concierge.models.SearchRequest;
import com.grookage.concierge.models.config.ConfigState;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class AerospikeManager {

    private final String namespace;
    private final AerospikeConfig aerospikeConfig;
    private final IAerospikeClient client;

    public AerospikeManager(AerospikeConfig aerospikeConfig) {
        this.namespace = aerospikeConfig.getNamespace();
        this.aerospikeConfig = aerospikeConfig;
        this.client = AerospikeClientUtils.getIClient(aerospikeConfig);
    }

    private Key getKey(
            final String recordKey
    ) {
        return new Key(namespace, AerospikeStorageConstants.CONFIG_SET, recordKey);
    }

    private Record getRecord(
            final Key key,
            final String bin
    ) {
        return client.get(null, key, bin);
    }

    @SneakyThrows
    private Collection<Bin> getBins(final AerospikeRecord aerospikeRecord) {
        final var bins = new ArrayList<Bin>();
        bins.add(new Bin(AerospikeStorageConstants.DEFAULT_BIN, AerospikeClientUtils.compress(
                MapperUtils.mapper().writeValueAsBytes(aerospikeRecord)))
        );
        bins.add(new Bin(AerospikeStorageConstants.NAMESPACE_BIN, aerospikeRecord.getConfigKey().getNamespace()));
        bins.add(new Bin(AerospikeStorageConstants.CONFIG_STATE_BIN, aerospikeRecord.getConfigState().name()));
        bins.add(new Bin(AerospikeStorageConstants.CONFIG_BIN, aerospikeRecord.getConfigKey().getConfigName()));
        bins.add(new Bin(AerospikeStorageConstants.ORG_BIN, aerospikeRecord.getConfigKey().getOrgId()));
        bins.add(new Bin(AerospikeStorageConstants.TENANT_BIN, aerospikeRecord.getConfigKey().getTenantId()));
        return bins;
    }

    public void save(AerospikeRecord aerospikeRecord) {
        final var key = getKey(aerospikeRecord.getConfigKey().getReferenceId());
        final var bins = getBins(aerospikeRecord).toArray(Bin[]::new);
        final var writePolicy = new WritePolicy(client.getWritePolicyDefault());
        writePolicy.recordExistsAction = RecordExistsAction.UPDATE;
        writePolicy.sendKey = true;
        client.put(writePolicy, key, bins);
    }

    @SneakyThrows
    public Optional<AerospikeRecord> getRecord(final String key) {
        final var recordKey = getKey(key);
        final var storedRecord = getRecord(recordKey, AerospikeStorageConstants.DEFAULT_BIN);
        if (null == storedRecord) return Optional.empty();
        final var aerospikeRecord = storedRecord.getBytes(AerospikeStorageConstants.DEFAULT_BIN);
        if (null == aerospikeRecord) return Optional.empty();
        return Optional.ofNullable(MapperUtils.mapper().readValue(
                AerospikeClientUtils.retrieve(aerospikeRecord),
                AerospikeRecord.class)
        );
    }

    private void augmentExpressions(final String binName,
                                    final Set<String> comparators,
                                    final List<Exp> searchableExpressions) {
        final var expressions = comparators.stream().map(comparator -> Exp.eq(Exp.stringBin(binName), Exp.val(comparator)))
                .toList();
        if (!expressions.isEmpty()) {
            if (expressions.size() == 1) {
                searchableExpressions.add(expressions.get(0));
            } else {
                searchableExpressions.add(Exp.or(expressions.toArray(Exp[]::new)));
            }
        }
    }

    @SneakyThrows
    public List<AerospikeRecord> getRecords(SearchRequest searchRequest) {
        final var queryStatement = new Statement();
        queryStatement.setNamespace(namespace);
        queryStatement.setBinNames(AerospikeStorageConstants.DEFAULT_BIN);
        queryStatement.setSetName(AerospikeStorageConstants.CONFIG_SET);
        queryStatement.setMaxRecords(10000);
        final var queryPolicy = client.copyQueryPolicyDefault();
        final var searchableExpressions = new ArrayList<Exp>();
        augmentExpressions(AerospikeStorageConstants.NAMESPACE_BIN, searchRequest.getNamespaces(), searchableExpressions);
        augmentExpressions(AerospikeStorageConstants.ORG_BIN, searchRequest.getOrgs(), searchableExpressions);
        augmentExpressions(AerospikeStorageConstants.TENANT_BIN, searchRequest.getTenants(), searchableExpressions);
        augmentExpressions(AerospikeStorageConstants.CONFIG_BIN, searchRequest.getConfigNames(), searchableExpressions);
        augmentExpressions(AerospikeStorageConstants.CONFIG_STATE_BIN, searchRequest.getConfigStates().stream().map(Enum::name).collect(Collectors.toSet()),
                        searchableExpressions);
        if (!searchableExpressions.isEmpty()) {
            if (searchableExpressions.size() == 1) {
                queryPolicy.setFilterExp(Exp.build(searchableExpressions.get(0)));
            } else {
                queryPolicy.setFilterExp(Exp.build(Exp.and(searchableExpressions.toArray(Exp[]::new))));
            }
        }
        final var aerospikeRecords = new ArrayList<AerospikeRecord>();
        try (final var rs = client.query(queryPolicy, queryStatement)) {
            while (rs.next()) {
                final var storageRecord = rs.getRecord();
                if (null != storageRecord) {
                    final var binRecord = storageRecord.getBytes(AerospikeStorageConstants.DEFAULT_BIN);
                    if (null != binRecord) {
                        aerospikeRecords.add(
                                MapperUtils.mapper().readValue(AerospikeClientUtils.retrieve(binRecord),
                                        AerospikeRecord.class)
                        );
                    }
                }
            }
        }
       return aerospikeRecords;
    }

    public void bulkUpdate(List<AerospikeRecord> aerospikeRecords) {
        final var transaction = new Txn();
        log.debug("Started transaction with id {} and records {}", transaction.getId(), aerospikeRecords);
        try {
            final var writePolicy = client.copyWritePolicyDefault();
            if (aerospikeConfig.isTxnEnabled()) {
                writePolicy.txn = transaction;
            }
            aerospikeRecords.forEach(storageRecord -> {
                final var key = getKey(storageRecord.getConfigKey().getReferenceId());
                final var bins = getBins(storageRecord).toArray(Bin[]::new);
                client.put(writePolicy, key, bins);
            });
        } catch (Exception e) {
            log.debug("There is an error trying to commit the transaction with id {}. Aborting the transaction", transaction.getId(), e);
            client.abort(transaction);
            throw e;
        } finally {
            client.commit(transaction);
        }
        log.debug("Successfully completed the transaction with id {} and records {}", transaction.getId(), aerospikeRecords);
    }

    public boolean exists(final String orgId,
                          final String configNamespace,
                          final String tenantId,
                          final String configName) {
        final var queryStatement = new Statement();
        queryStatement.setNamespace(namespace);
        queryStatement.setBinNames(AerospikeStorageConstants.DEFAULT_BIN);
        queryStatement.setSetName(AerospikeStorageConstants.CONFIG_SET);
        final var queryPolicy = client.copyQueryPolicyDefault();
        queryPolicy.filterExp = Exp.build(Exp.and(
                Exp.eq(Exp.stringBin(AerospikeStorageConstants.ORG_BIN), Exp.val(orgId)),
                Exp.eq(Exp.stringBin(AerospikeStorageConstants.NAMESPACE_BIN), Exp.val(configNamespace)),
                Exp.eq(Exp.stringBin(AerospikeStorageConstants.TENANT_BIN), Exp.val(tenantId)),
                Exp.eq(Exp.stringBin(AerospikeStorageConstants.CONFIG_BIN), Exp.val(configName)),
                Exp.eq(Exp.stringBin(AerospikeStorageConstants.CONFIG_STATE_BIN), Exp.val(ConfigState.CREATED.name()))
        ));
        try (final var resultSet = client.query(queryPolicy, queryStatement)) {
            var resultCount = 0;
            while (resultSet.next()) {
                if (null != resultSet.getRecord()) {
                    resultCount++;
                }
            }
            return resultCount > 0;
        }
    }
}
