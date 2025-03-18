package com.grookage.concierge.core.services;

import com.grookage.concierge.models.SearchRequest;
import com.grookage.concierge.models.config.ConciergeRequestContext;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;

import java.util.List;
import java.util.Optional;

public interface ConfigService {

    Optional<ConfigDetails> getConfig(ConciergeRequestContext requestContext, ConfigKey configKey);

    List<ConfigDetails> getConfigs(ConciergeRequestContext requestContext, SearchRequest searchRequest);

}
