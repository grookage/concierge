package com.grookage.concierge.core.utils;

import lombok.experimental.UtilityClass;

import java.util.Collection;

@UtilityClass
public class CollectionUtils {

    public boolean isNullOrEmpty(Collection<?> collection) {
        return null == collection || collection.isEmpty();
    }

}
