package com.grookage.concierge.repository.cache;

import com.grookage.leia.provider.refresher.AbstractLeiaRefresher;
import lombok.Builder;

public class RepositoryRefresher extends AbstractLeiaRefresher<ConfigRegistry> {

    @Builder
    protected RepositoryRefresher(RepositorySupplier supplier,
                                  int dataRefreshInterval) {
        super(supplier, dataRefreshInterval, true);
    }
}
