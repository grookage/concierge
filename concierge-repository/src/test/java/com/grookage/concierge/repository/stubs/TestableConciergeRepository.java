package com.grookage.concierge.repository.stubs;

import com.grookage.concierge.models.ResourceHelper;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.config.ConfigState;
import com.grookage.concierge.repository.AbstractConciergeRepository;
import com.grookage.concierge.repository.cache.CacheConfig;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
public class TestableConciergeRepository extends AbstractConciergeRepository {

    private static List<ConfigDetails> CONFIG_DETAILS;

    static {
        try {
            CONFIG_DETAILS = Collections.singletonList(ResourceHelper
                    .getResource("configDetails.json", ConfigDetails.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TestableConciergeRepository(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    public void resetConfigDetails() {
        CONFIG_DETAILS = List.of();
    }

    @Override
    public List<ConfigDetails> getStoredRecords(String namespace, Set<String> configNames, Set<ConfigState> configStates) {
        return CONFIG_DETAILS;
    }

    @Override
    public List<ConfigDetails> getActiveStoredRecords(Set<String> namespaces) {
        return CONFIG_DETAILS.stream().filter(each -> each.getConfigState() == ConfigState.ACTIVATED).toList();
    }

    @Override
    public List<ConfigDetails> getStoredRecords(Set<String> namespaces) {
        return CONFIG_DETAILS;
    }

    @Override
    public void create(ConfigDetails configDetails) {

    }

    @Override
    public void update(ConfigDetails configDetails) {

    }

    @Override
    public Optional<ConfigDetails> getStoredRecord(ConfigKey configKey) {
        return CONFIG_DETAILS.stream().findFirst();
    }

    @Override
    public boolean createdRecordExists(String namespace, String configName) {
        return false;
    }

    @Override
    public List<ConfigDetails> getStoredRecords() {
        return CONFIG_DETAILS;
    }

    @Override
    public void rollOverAndUpdate(ConfigDetails configDetails) {

    }
}
