package com.grookage.concierge.core.cache;

import com.grookage.korg.consumer.KorgConsumer;
import com.grookage.korg.refresher.AbstractKorgRefresher;
import lombok.Builder;
import lombok.Getter;

import java.util.function.Supplier;

@Getter
public class RepositoryRefresher extends AbstractKorgRefresher<ConfigRegistry> {

    private final Supplier<KorgConsumer<ConfigRegistry>> consumerSupplier;

    @Builder
    protected RepositoryRefresher(RepositorySupplier supplier,
                                  int dataRefreshInterval,
                                  boolean periodicRefresh,
                                  Supplier<KorgConsumer<ConfigRegistry>> consumerSupplier) {
        super(supplier, dataRefreshInterval, periodicRefresh, consumerSupplier);
        this.consumerSupplier = consumerSupplier;
    }
}
