package com.grookage.concierge.core.cache;

import com.grookage.leia.provider.refresher.AbstractLeiaRefresher;
import lombok.Builder;

public class RepositoryRefresher extends AbstractLeiaRefresher<ConfigRegistry> {

    @Builder
    protected RepositoryRefresher(RepositorySupplier supplier,
                                  int dataRefreshInterval,
                                  boolean periodicRefresh) {
        super(supplier, dataRefreshInterval, periodicRefresh);
    }
}
