package com.grookage.concierge.models.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigHistoryItem {

    @NotNull ConfigEvent configEvent;
    @NotNull long timestamp;
    @NotBlank String configUpdaterName;
    String configUpdaterId;
    String configUpdaterEmail;

    @Override
    public int hashCode() {
        return this.getConfigEvent().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        final var thatKey = (ConfigHistoryItem) obj;
        return (thatKey.getConfigEvent().equals(this.configEvent));
    }
}
