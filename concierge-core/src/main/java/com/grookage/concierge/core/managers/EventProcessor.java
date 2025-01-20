package com.grookage.concierge.core.managers;

import com.grookage.concierge.core.engine.ConciergeContext;

public interface EventProcessor {

    void preProcess(ConciergeContext context);

    void postProcess(ConciergeContext context);

}
