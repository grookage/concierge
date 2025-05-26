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

    @NotNull
    ConfigEvent configEvent;
    @NotNull
    long timestamp;
    @NotBlank
    String configUpdaterName;
    @NotBlank
    String configUpdaterId;
    String configUpdaterEmail;
    String message;
}
