package com.grookage.concierge.server.aerospike;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.grookage.concierge.aerospike.client.AerospikeConfig;
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
public class AerospikeAppConfiguration extends Configuration {

    @NotEmpty
    @NotNull
    private String name;

    @NotNull
    private AerospikeConfig aerospikeConfig;
}
