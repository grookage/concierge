package com.grookage.concierge.core.engine.resolver;

import com.grookage.concierge.models.ingestion.UpdateConfigRequest;

/**
 * This interface takes in the updateConfigRequest, does the required merge
 * with the existing ones and returns the responsible object
 */
public interface AppendConfigResolver {

    Object merge(UpdateConfigRequest updateConfigRequest);

}
