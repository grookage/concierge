package com.grookage.concierge.models.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Joiner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Locale;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigKey implements Comparable<ConfigKey> {

    @NotBlank
    String namespace;
    @NotBlank
    String configName;
    @NotBlank
    String version;
    ConfigType configType;

    @JsonIgnore
    public String getReferenceId() {
        return Joiner.on(".").join(namespace, configName, version).toUpperCase(Locale.ROOT);
    }

    @JsonIgnore
    public String getReferenceTag() {
        return Joiner.on(".").join(namespace, configName).toUpperCase(Locale.ROOT);
    }

    @JsonIgnore
    public boolean latest() {
        return null != version && version.equalsIgnoreCase("latest");
    }

    @Override
    public int hashCode() {
        return this.getReferenceId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        final var thatKey = (ConfigKey) obj;
        return (thatKey.getReferenceId().equals(this.getReferenceId()));
    }

    @Override
    public int compareTo(ConfigKey o) {
        return this.version.compareTo(o.getVersion());
    }
}
