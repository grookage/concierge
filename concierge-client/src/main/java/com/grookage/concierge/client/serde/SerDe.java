package com.grookage.concierge.client.serde;

import com.grookage.concierge.models.ingestion.ConfigurationResponse;

public interface SerDe<T> {

    T convert(ConfigurationResponse configurationResponse);

}
