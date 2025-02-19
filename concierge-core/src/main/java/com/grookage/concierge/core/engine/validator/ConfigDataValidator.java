package com.grookage.concierge.core.engine.validator;

import com.grookage.concierge.models.config.ConfigKey;

public interface ConfigDataValidator {

    void validate(ConfigKey configKey, Object configData);

}
