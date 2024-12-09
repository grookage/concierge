package com.grookage.concierge.core.stubs;

import com.grookage.concierge.models.ConfigUpdater;

public class TestConfigUpdater implements ConfigUpdater {
    @Override
    public String name() {
        return "name";
    }

    @Override
    public String userId() {
        return "userId";
    }

    @Override
    public String email() {
        return "email";
    }
}
