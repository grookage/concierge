package com.grookage.concierge.aerospike.repository;

import com.grookage.concierge.aerospike.client.AerospikeConfig;
import com.grookage.concierge.aerospike.client.AerospikeManager;
import com.grookage.concierge.aerospike.storage.AerospikeRecord;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.config.ConfigState;
import com.grookage.concierge.repository.ConciergeRepository;
import lombok.Getter;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class AerospikeRepository implements ConciergeRepository {

    private final AerospikeManager aerospikeManager;

    public AerospikeRepository(AerospikeConfig aerospikeConfig) {
        this.aerospikeManager = new AerospikeManager(aerospikeConfig);
    }

    private AerospikeRecord toStorageRecord(ConfigDetails configDetails) {
        return AerospikeRecord.builder()
                .data(configDetails.getData())
                .configHistories(configDetails.getConfigHistories())
                .configState(configDetails.getConfigState())
                .version(configDetails.getConfigKey().getVersion())
                .description(configDetails.getDescription())
                .namespace(configDetails.getConfigKey().getNamespace())
                .configName(configDetails.getConfigKey().getConfigName())
                .configType(configDetails.getConfigKey().getConfigType())
                .build();
    }

    private ConfigDetails toConfigDetails(AerospikeRecord aerospikeRecord) {
        return ConfigDetails.builder()
                .data(aerospikeRecord.getData())
                .configHistories(aerospikeRecord.getConfigHistories())
                .configState(aerospikeRecord.getConfigState())
                .description(aerospikeRecord.getDescription())
                .configKey(ConfigKey.builder()
                        .version(aerospikeRecord.getVersion())
                        .namespace(aerospikeRecord.getNamespace())
                        .configName(aerospikeRecord.getConfigName())
                        .configType(aerospikeRecord.getConfigType())
                        .build())
                .build();
    }

    @Override
    public void create(ConfigDetails configDetails) {
        aerospikeManager.create(toStorageRecord(configDetails));
    }

    @Override
    public void update(ConfigDetails configDetails) {
        aerospikeManager.update(toStorageRecord(configDetails));
    }

    @Override
    public Optional<ConfigDetails> getStoredRecord(ConfigKey configKey) {
        return aerospikeManager.getRecord(configKey.getReferenceId())
                .map(this::toConfigDetails);
    }

    @Override
    public boolean createdRecordExists(String namespace, String configName) {
        return aerospikeManager.exists(namespace, configName, ConfigState.CREATED.name());
    }

    @Override
    public List<ConfigDetails> getStoredRecords() {
        return aerospikeManager.getRecords(Set.of(), Set.of(), Set.of())
                .stream().map(this::toConfigDetails).toList();
    }

    @Override
    public List<ConfigDetails> getStoredRecords(Set<String> namespaces, Set<String> configNames, Set<ConfigState> configStates) {
        return aerospikeManager.getRecords(namespaces, configNames,
                        configStates.stream().map(Enum::name).collect(Collectors.toSet()))
                .stream().map(this::toConfigDetails).toList();
    }

    @Override
    public void rollOverAndUpdate(ConfigDetails configDetails) {
        final var newRecords = aerospikeManager.getRecords(Set.of(configDetails.getConfigKey().getNamespace()),
                        Set.of(configDetails.getConfigKey().getConfigName()),
                        Set.of(ConfigState.ACTIVATED.name()))
                .stream().peek(each -> each.setConfigState(ConfigState.ROLLED)).collect(Collectors.toList());
        newRecords.add(toStorageRecord(configDetails));
        aerospikeManager.bulkUpdate(newRecords);
    }
}
