package com.grookage.concierge.repository;

import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.config.ConfigState;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ConciergeRepository {

    void create(ConfigDetails configDetails);

    void update(ConfigDetails configDetails);

    Optional<ConfigDetails> getStoredRecord(final ConfigKey configKey);

    boolean createdRecordExists(String namespace, String configName);

    List<ConfigDetails> getStoredRecords();

    List<ConfigDetails> getStoredRecords(Set<String> namespace, Set<String> configNames, Set<ConfigState> configStates);

    List<ConfigDetails> getActiveStoredRecords(Set<String> namespaces);

    void rollOverAndUpdate(ConfigDetails configDetails);

}
