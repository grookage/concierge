package com.grookage.concierge.core.cache;

import com.grookage.concierge.models.SearchRequest;
import com.grookage.concierge.repository.ConciergeRepository;
import com.grookage.korg.suppliers.KorgSupplier;
import lombok.AllArgsConstructor;

import java.util.Set;
import java.util.function.Supplier;


@AllArgsConstructor
public class RepositorySupplier implements KorgSupplier<ConfigRegistry> {

    private final Supplier<ConciergeRepository> repositorySupplier;
    private final Set<String> configTypes;

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
        final var searchRequest = SearchRequest.builder()
                .configTypes(configTypes)
                .build();
        final var configDetails = repositorySupplier.get().getStoredRecords(searchRequest);
        final var registry = new ConfigRegistry();
        configDetails.forEach(registry::add);
        return registry;
    }
}
