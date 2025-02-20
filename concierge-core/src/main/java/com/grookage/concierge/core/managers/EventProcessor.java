package com.grookage.concierge.core.managers;

import com.grookage.concierge.core.engine.ConciergeContext;
import com.grookage.concierge.models.processor.ProcessorKey;

public interface EventProcessor {

    void preProcess(ProcessorKey processorKey, ConciergeContext context);

    void postProcess(ProcessorKey processorKey, ConciergeContext context);

}
