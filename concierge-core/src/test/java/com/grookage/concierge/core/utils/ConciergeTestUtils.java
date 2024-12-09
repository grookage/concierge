package com.grookage.concierge.core.utils;

import com.google.common.base.Joiner;
import com.grookage.concierge.core.managers.VersionGenerator;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConciergeTestUtils {
    public static final VersionGenerator generator =
            prefix -> Joiner.on(".").join(prefix, System.currentTimeMillis());
}
