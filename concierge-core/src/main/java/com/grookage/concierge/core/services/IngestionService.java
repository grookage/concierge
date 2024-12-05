package com.grookage.concierge.core.services;

import com.grookage.concierge.models.ConfigUpdater;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.ingestion.ConfigurationRequest;
import com.grookage.concierge.models.ingestion.UpdateConfigRequest;

public interface IngestionService<C extends ConfigUpdater> {

    ConfigDetails createConfiguration(C configUpdater, ConfigurationRequest configurationRequest);

    ConfigDetails updateConfiguration(C configUpdater, UpdateConfigRequest configurationRequest);

    ConfigDetails approveConfiguration(C configUpdater, ConfigKey configKey);

    ConfigDetails rejectConfiguration(C configUpdater, ConfigKey configKey);

    ConfigDetails activateConfiguration(C configUpdater, ConfigKey configKey);

}
