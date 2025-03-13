package com.grookage.concierge.aerospikedw;

import com.google.common.base.Preconditions;
import com.grookage.conceirge.dwserver.ConciergeBundle;
import com.grookage.concierge.aerospike.client.AerospikeConfig;
import com.grookage.concierge.aerospike.repository.AerospikeRepository;
import com.grookage.concierge.models.ConfigUpdater;
import com.grookage.concierge.repository.ConciergeRepository;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

@NoArgsConstructor
@Getter
public abstract class ConciergeAerospikeBundle<T extends Configuration, U extends ConfigUpdater> extends ConciergeBundle<T, U> {


    private AerospikeRepository aerospikeRepository;

    protected abstract AerospikeConfig getAerospikeConfig(T configuration);

    @Override
    protected Supplier<ConciergeRepository> getRepositorySupplier(T configuration) {
        return () -> aerospikeRepository;
    }

    @Override
    public void run(T configuration, Environment environment) {
        final var aerospikeConfig = getAerospikeConfig(configuration);
        Preconditions.checkNotNull(aerospikeConfig, "Aerospike Config can't be null");
        this.aerospikeRepository = new AerospikeRepository(aerospikeConfig);
        super.run(configuration, environment);
    }
}
