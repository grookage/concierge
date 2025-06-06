/*
 * Copyright (c) 2024. Koushik R <rkoushik.14@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.grookage.concierge.core.utils;

import com.grookage.concierge.core.engine.ConciergeContext;
import com.grookage.concierge.models.ConfigUpdater;
import com.grookage.concierge.models.exception.ConciergeCoreErrorCode;
import com.grookage.concierge.models.exception.ConciergeException;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class ContextUtils {

    private static final String USER_NAME = "USER_NAME";
    private static final String EMAIL = "EMAIL";

    private static final String USER_ID = "USER_ID";

    public static void addConfigUpdaterContext(final ConciergeContext conciergeContext,
                                               final ConfigUpdater configUpdater) {
        conciergeContext.addContext(USER_NAME, configUpdater.name());
        conciergeContext.addContext(EMAIL, configUpdater.email());
        conciergeContext.addContext(USER_ID, configUpdater.userId());
    }

    @SneakyThrows
    public static String getUser(final ConciergeContext conciergeContext) {
        return conciergeContext.getValue(USER_NAME)
                .orElseThrow((Supplier<Throwable>) () -> ConciergeException.error(ConciergeCoreErrorCode.USER_NOT_FOUND));
    }

    @SneakyThrows
    public static String getEmail(final ConciergeContext conciergeContext) {
        return conciergeContext.getValue(EMAIL)
                .orElseThrow((Supplier<Throwable>) () -> ConciergeException.error(ConciergeCoreErrorCode.USER_NOT_FOUND));
    }

    @SneakyThrows
    public static String getUserId(final ConciergeContext conciergeContext) {
        return conciergeContext.getValue(USER_ID)
                .orElseThrow((Supplier<Throwable>) () -> ConciergeException.error(ConciergeCoreErrorCode.USER_NOT_FOUND));
    }
}
