package com.grookage.concierge.server.elastic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.grookage.concierge.elastic.config.ElasticConfig;
import io.dropwizard.Configuration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class ElasticAppConfiguration extends Configuration {

    @NotEmpty
    @NotNull
    private String name;

    @NotNull
    private ElasticConfig elasticConfig;
}
