package com.grookage.concierge.client.serde;

import com.grookage.concierge.models.config.ConfigKey;

public interface SerDeFactory {

    <T> SerDe<T> getSerDe(ConfigKey configKey);

}
