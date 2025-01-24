package com.grookage.concierge.models.ingestion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.grookage.concierge.models.config.ConfigHistoryItem;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.config.ConfigState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigurationResponse implements Comparable<ConfigurationResponse> {

    @NotNull ConfigKey configKey;
    @NotNull ConfigState configState;
    String description;
    @NotNull Object data;
    @Builder.Default
    Set<ConfigHistoryItem> configHistories = new HashSet<>();

    @Override
    public int compareTo(ConfigurationResponse o) {
        return configKey.compareTo(o.getConfigKey());
    }
}
