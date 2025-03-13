package com.grookage.concierge.core.services;

import com.grookage.concierge.models.config.ConciergeRequestContext;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ConfigService {

    Optional<ConfigDetails> getConfig(ConciergeRequestContext requestContext, ConfigKey configKey);

    Optional<ConfigDetails> getLatestActiveConfig(ConciergeRequestContext requestContext,
                                                  String namespace,
                                                  String configName);

    List<ConfigDetails> getConfigs(ConciergeRequestContext requestContext,
                                   Set<String> namespaces);

    List<ConfigDetails> getConfigs(ConciergeRequestContext requestContext,
                                   String namespace,
                                   Set<String> configNames);

    List<ConfigDetails> getActiveConfigs(ConciergeRequestContext requestContext,
                                         Set<String> namespaces);

    List<ConfigDetails> getConfigs(ConciergeRequestContext requestContext);

}
