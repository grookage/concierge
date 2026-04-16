package com.grookage.concierge.core.engine.resolver;

import com.grookage.concierge.models.config.ConfigKey;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DefaultConfigVersionManager implements ConfigVersionManager {
    @Override
    public boolean enableMultipleConfigs(String configType) {
        return false;
    }

    @Override
    public String generateConfigVersion(ConfigKey configKey) {
        return configKey.getVersion();
    }
}
