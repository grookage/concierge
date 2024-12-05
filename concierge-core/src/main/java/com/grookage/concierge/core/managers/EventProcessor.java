package com.grookage.concierge.core.managers;

import com.grookage.concierge.core.engine.ConciergeContext;

public interface EventProcessor {

    void process(ConciergeContext context);

}
