package com.grookage.concierge.core.engine.resolver;

import com.grookage.concierge.models.ingestion.UpdateConfigRequest;

public class DefaultAppendConfigResolver implements AppendConfigResolver {
    @Override
    public Object merge(UpdateConfigRequest updateConfigRequest) {
        return updateConfigRequest.getData();
    }

}
