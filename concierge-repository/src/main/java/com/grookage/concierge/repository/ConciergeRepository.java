package com.grookage.concierge.repository;

import com.grookage.concierge.models.SearchRequest;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ConciergeRepository {

    void create(ConfigDetails configDetails);

    void update(ConfigDetails configDetails);

    Optional<ConfigDetails> getStoredRecord(final String configType, final String referenceId);

    default Optional<ConfigDetails> getStoredRecord(final ConfigKey configKey) {
        return getStoredRecord(configKey.getConfigType(), configKey.getReferenceId());
    }

    default List<ConfigDetails> getStoredRecords(final String configType) {
        final var searchRequest = SearchRequest.builder()
                .configTypes(Set.of(configType))
                .build();
        return getStoredRecords(searchRequest);
    }

    List<ConfigDetails> getStoredRecords(SearchRequest searchRequest);

    void rollOverAndUpdate(ConfigDetails configDetails);

}
