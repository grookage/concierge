package com.grookage.concierge.models.ingestion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class UpdateConfigRequest {
    @NotBlank String namespace;
    @NotBlank String configName;
    @NotBlank String version;
    String description;
    @NotNull byte[] data;
}
