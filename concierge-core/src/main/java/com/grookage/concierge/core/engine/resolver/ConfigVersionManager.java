package com.grookage.concierge.core.engine.resolver;

import com.grookage.concierge.models.config.ConfigType;

public interface ConfigVersionManager {

    boolean enableMultipleConfigs(ConfigType configType);

}
