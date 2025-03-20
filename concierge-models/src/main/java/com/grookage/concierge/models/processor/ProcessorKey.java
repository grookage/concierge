package com.grookage.concierge.models.processor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.config.ConfigKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessorKey {

    @NotNull
    ConfigKey configKey;
    @NotNull
    private ConfigEvent configEvent;
}
