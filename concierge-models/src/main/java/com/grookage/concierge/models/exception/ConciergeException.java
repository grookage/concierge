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

package com.grookage.concierge.models.exception;

import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ConciergeException extends RuntimeException {

    private final int status;
    private final String code;
    private final transient Map<String, Object> context;

    @Builder
    private ConciergeException(ConciergeErrorCode errorCode, Map<String, Object> context) {
        super();

        this.status = errorCode.getStatus();
        this.code = errorCode.name();
        this.context = context;
    }

    private ConciergeException(ConciergeErrorCode errorCode, Throwable cause) {
        super(cause);

        this.code = errorCode.name();
        this.status = errorCode.getStatus();
        this.context = cause != null && cause.getLocalizedMessage() != null ?
                Map.of("message", cause.getLocalizedMessage()) : new HashMap<>();
    }

    public static ConciergeException error(ConciergeErrorCode errorCode, Throwable t) {
        return new ConciergeException(errorCode, t);
    }

    public static ConciergeException error(ConciergeErrorCode errorCode) {
        return new ConciergeException(errorCode, new HashMap<>());
    }

}
