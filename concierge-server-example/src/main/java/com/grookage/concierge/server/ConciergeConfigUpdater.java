package com.grookage.concierge.server;

import com.grookage.concierge.models.ConfigUpdater;

public class ConciergeConfigUpdater implements ConfigUpdater {
    @Override
    public String name() {
        return "Concierge";
    }

    @Override
    public String userId() {
        return "U12242134123423";
    }

    @Override
    public String email() {
        return "concierge@grookage.com";
    }
}
