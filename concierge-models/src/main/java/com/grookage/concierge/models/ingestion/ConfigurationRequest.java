package com.grookage.concierge.models.ingestion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.config.ConfigType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigurationRequest {
    @NotBlank
    String namespace;
    @NotBlank
    String configName;
    @NotBlank
    String versionId;
    String description;
    @NotNull
    Object data;
    ConfigType configType;
    String message;

    @JsonIgnore
    public ConfigKey getConfigKey() {
        return ConfigKey.builder()
                .namespace(namespace)
                .configName(configName)
                .configType(configType)
                .version(versionId)
                .build();
    }
}
