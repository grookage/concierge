package com.grookage.concierge.core.managers;

import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.processor.ProcessorKey;

import java.util.Optional;

public interface ProcessorFactory {

    Optional<EventProcessor> getProcessor(final ProcessorKey processorKey);

}
