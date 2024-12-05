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

package com.grookage.conceirge.dwserver.permissions;

import com.grookage.concierge.models.ConfigUpdater;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.ingestion.ConfigurationRequest;
import com.grookage.concierge.models.ingestion.UpdateConfigRequest;

import javax.ws.rs.core.HttpHeaders;

public interface PermissionValidator<U extends ConfigUpdater> {

    void authorize(final HttpHeaders headers,
                   final U schemaUpdater,
                   final ConfigurationRequest configurationRequest);

    void authorize(final HttpHeaders headers,
                   final U schemaUpdater,
                   final UpdateConfigRequest configRequest);

    void authorizeApproval(final HttpHeaders headers,
                           final U schemaUpdater,
                           final ConfigKey schemaKey);

    void authorizeRejection(final HttpHeaders headers,
                            final U schemaUpdater,
                            final ConfigKey schemaKey);

    void authorizeActivation(final HttpHeaders headers,
                             final U schemaUpdater,
                             final ConfigKey schemaKey);

}
