package com.grookage.concierge.elastic.storage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.grookage.concierge.models.config.ConfigHistoryItem;
import com.grookage.concierge.models.config.ConfigState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoredElasticRecord {
    @NotBlank String namespace;
    @NotBlank String configName;
    @NotBlank String version;
    @NotNull ConfigState configState;
    String description;
    @NotNull byte[] data;
    @Builder.Default
    Set<ConfigHistoryItem> configHistories = new HashSet<>();
}
