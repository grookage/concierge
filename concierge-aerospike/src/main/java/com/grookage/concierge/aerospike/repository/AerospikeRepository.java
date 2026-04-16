package com.grookage.concierge.aerospike.repository;

import com.grookage.concierge.aerospike.client.AerospikeConfig;
import com.grookage.concierge.aerospike.client.AerospikeManager;
import com.grookage.concierge.aerospike.storage.AerospikeRecord;
import com.grookage.concierge.models.MapperUtils;
import com.grookage.concierge.models.SearchRequest;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigState;
import com.grookage.concierge.repository.ConciergeRepository;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class AerospikeRepository implements ConciergeRepository {

    private final AerospikeManager aerospikeManager;

    public AerospikeRepository(AerospikeConfig aerospikeConfig) {
        this.aerospikeManager = new AerospikeManager(aerospikeConfig);
    }

    @SneakyThrows
    private AerospikeRecord toStorageRecord(ConfigDetails configDetails) {
        return AerospikeRecord.builder()
                .data(MapperUtils.mapper().writeValueAsBytes(configDetails))
                .configKey(configDetails.getConfigKey())
                .configState(configDetails.getConfigState())
                .build();
    }

    @SneakyThrows
    private ConfigDetails toConfigDetails(AerospikeRecord aerospikeRecord) {
        return MapperUtils.mapper().readValue(aerospikeRecord.getData(), ConfigDetails.class);
    }

    @Override
    public void create(ConfigDetails configDetails) {
        aerospikeManager.save(toStorageRecord(configDetails));
    }

    @Override
    public void update(ConfigDetails configDetails) {
        aerospikeManager.save(toStorageRecord(configDetails));
    }

    @Override
    public Optional<ConfigDetails> getStoredRecord(String configType, String referenceId) {
        return aerospikeManager.getRecord(referenceId)
                .map(this::toConfigDetails);
    }

    @Override
    public List<ConfigDetails> getStoredRecords(SearchRequest searchRequest) {
        return aerospikeManager.getRecords(searchRequest)
                .stream().map(this::toConfigDetails).toList();
    }

    @Override
    public void rollOverAndUpdate(ConfigDetails configDetails) {
        final var searchRequest = SearchRequest.builder()
                .orgs(Set.of(configDetails.getConfigKey().getOrgId()))
                .namespaces(Set.of(configDetails.getConfigKey().getNamespace()))
                .tenants(Set.of(configDetails.getConfigKey().getTenantId()))
                .configNames(Set.of(configDetails.getConfigKey().getConfigName()))
                .configTypes(Set.of(configDetails.getConfigKey().getConfigType()))
                .configStates(Set.of(ConfigState.ACTIVATED))
                .build();
        final var newRecords = getStoredRecords(searchRequest)
                .stream()
                .peek(each -> each.setConfigState(ConfigState.ROLLED))
                .map(this::toStorageRecord)
                .collect(Collectors.toList());
        newRecords.add(toStorageRecord(configDetails));
        aerospikeManager.bulkUpdate(newRecords);
    }
}
