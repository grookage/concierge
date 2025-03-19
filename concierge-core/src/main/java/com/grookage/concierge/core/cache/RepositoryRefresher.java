package com.grookage.concierge.core.cache;

import com.grookage.korg.refresher.AbstractKorgRefresher;
import lombok.Builder;

public class RepositoryRefresher extends AbstractKorgRefresher<ConfigRegistry> {

    @Builder
    protected RepositoryRefresher(RepositorySupplier supplier,
                                  int dataRefreshInterval,
                                  boolean periodicRefresh) {
        super(supplier, dataRefreshInterval, periodicRefresh);
    }
}
