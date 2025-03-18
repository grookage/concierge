package com.grookage.concierge.models.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigDetails implements Comparable<ConfigDetails> {
    @NotNull
    ConfigKey configKey;
    @NotNull
    ConfigState configState;
    String description;
    @NotNull
    byte[] data;
    @Builder.Default
    Set<ConfigHistoryItem> configHistories = new HashSet<>();

    @JsonIgnore
    public String getReferenceId() {
        return configKey.getReferenceId();
    }

    @JsonIgnore
    public void addHistory(ConfigHistoryItem configHistoryItem) {
        if (null == configHistories) {
            configHistories = new HashSet<>();
        }

        configHistories.add(configHistoryItem);
    }

    @Override
    public int compareTo(ConfigDetails o) {
        return configKey.compareTo(o.getConfigKey());
    }

}
