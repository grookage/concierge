package com.grookage.concierge.models.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigMeta {

    @NotEmpty
    private String createdBy;
    private String createdByEmail;
    private String createdByUserId;
    private long createdAt;
    private String updatedBy;
    private String updatedByEmail;
    private String updatedByUserId;
    private long updatedAt;

}
