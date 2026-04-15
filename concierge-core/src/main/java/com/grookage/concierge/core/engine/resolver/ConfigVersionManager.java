package com.grookage.concierge.core.engine.resolver;

import com.grookage.concierge.models.config.ConfigKey;

public interface ConfigVersionManager {

    boolean enableMultipleConfigs(String configType);

    String generateConfigVersion(ConfigKey configKey);
}
