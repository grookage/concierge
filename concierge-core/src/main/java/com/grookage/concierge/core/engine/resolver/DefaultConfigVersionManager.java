package com.grookage.concierge.core.engine.resolver;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DefaultConfigVersionManager implements ConfigVersionManager {
    @Override
    public boolean enableMultipleConfigs(String configType) {
        return false;
    }
}
