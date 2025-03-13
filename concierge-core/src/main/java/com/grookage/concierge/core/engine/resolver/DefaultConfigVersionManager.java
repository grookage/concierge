package com.grookage.concierge.core.engine.resolver;

import com.grookage.concierge.models.config.ConfigType;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DefaultConfigVersionManager implements ConfigVersionManager {
    @Override
    public boolean enableMultipleConfigs(ConfigType configType) {
        return false;
    }
}
