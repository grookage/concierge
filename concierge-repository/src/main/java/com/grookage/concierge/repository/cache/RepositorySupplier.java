package com.grookage.concierge.repository.cache;

import com.grookage.concierge.repository.ConciergeRepository;
import com.grookage.leia.provider.suppliers.LeiaSupplier;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class RepositorySupplier implements LeiaSupplier<ConfigRegistry> {

    private final ConciergeRepository repository;

    @Override
    public void start() {
        //NOOP
    }

    @Override
    public void stop() {
        //NOOP
    }

    @Override
    public ConfigRegistry get() {
        final var configDetails = repository.getStoredRecords();
        final var registry = new ConfigRegistry();
        configDetails.forEach(registry::add);
        return registry;
    }
}
