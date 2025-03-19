package com.grookage.concierge.models;

import lombok.experimental.UtilityClass;

import java.util.Collection;

@UtilityClass
public class CollectionUtils {

    public boolean isNullOrEmpty(Collection<?> collection) {
        return null == collection || collection.isEmpty();
    }

}
