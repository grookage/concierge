package com.grookage.concierge.repository;

import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ConciergeRepository {

    void create(ConfigDetails configDetails);

    void update(ConfigDetails configDetails);

    void rollOverAndUpdate(ConfigDetails configDetails);

    Optional<ConfigDetails> getStoredRecord(final ConfigKey configKey);

    boolean createdRecordExists(String namespace, String configName);

    List<ConfigDetails> getStoredRecords();

    //The following implementations have cache binding, if enabled.
    Optional<ConfigDetails> getRecord(final ConfigKey configKey);

    List<ConfigDetails> getRecords(final String namespace, final Set<String> configNames);

    List<ConfigDetails> getActiveRecords(final Set<String> namespaces);

    List<ConfigDetails> getRecords(final Set<String> namespaces);

    List<ConfigDetails> getRecords();

}
