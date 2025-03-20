package com.grookage.concierge.repository;

import com.grookage.concierge.models.SearchRequest;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;

import java.util.List;
import java.util.Optional;

public interface ConciergeRepository {

    void create(ConfigDetails configDetails);

    void update(ConfigDetails configDetails);

    Optional<ConfigDetails> getStoredRecord(final String referenceId);

    default Optional<ConfigDetails> getStoredRecord(final ConfigKey configKey) {
        return getStoredRecord(configKey.getReferenceId());
    }

    boolean createdRecordExists(ConfigKey configKey);

    List<ConfigDetails> getStoredRecords();

    List<ConfigDetails> getStoredRecords(SearchRequest searchRequest);

    void rollOverAndUpdate(ConfigDetails configDetails);

}
