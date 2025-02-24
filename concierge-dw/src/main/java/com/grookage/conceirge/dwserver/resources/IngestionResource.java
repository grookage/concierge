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

package com.grookage.conceirge.dwserver.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.grookage.conceirge.dwserver.permissions.PermissionValidator;
import com.grookage.conceirge.dwserver.resolvers.ConfigUpdaterResolver;
import com.grookage.concierge.core.engine.validator.ConfigDataValidator;
import com.grookage.concierge.core.services.IngestionService;
import com.grookage.concierge.models.ConfigUpdater;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.ingestion.ConfigurationRequest;
import com.grookage.concierge.models.ingestion.UpdateConfigRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.security.PermitAll;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.function.Supplier;

@Singleton
@Getter
@Setter
@Path("/v1/ingest")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@AllArgsConstructor
@PermitAll
public class IngestionResource<U extends ConfigUpdater> {

    private final IngestionService<U> ingestionService;
    private final Supplier<ConfigUpdaterResolver<U>> updaterResolver;
    private final Supplier<PermissionValidator<U>> permissionValidatorSupplier;
    private final Supplier<ConfigDataValidator> configDataValidatorSupplier;

    @PUT
    @Timed
    @ExceptionMetered
    @Path("/add")
    public ConfigDetails addConfig(@Context HttpHeaders headers,
                                   @Valid final ConfigurationRequest configurationRequest) {
        final var updater = updaterResolver.get().resolve(headers);
        permissionValidatorSupplier.get().authorize(headers, updater, configurationRequest);
        configDataValidatorSupplier.get().validate(configurationRequest.getConfigKey(), configurationRequest.getData());
        return ingestionService.createConfiguration(updater, configurationRequest);
    }

    @PATCH
    @Timed
    @ExceptionMetered
    @Path("/append")
    public ConfigDetails appendConfig(@Context HttpHeaders headers,
                                      @Valid final UpdateConfigRequest updateRequest) {
        final var updater = updaterResolver.get().resolve(headers);
        permissionValidatorSupplier.get().authorize(headers, updater, updateRequest);
        configDataValidatorSupplier.get().validate(updateRequest.getConfigKey(), updateRequest.getData());
        return ingestionService.appendConfiguration(updater, updateRequest);
    }

    @PUT
    @Timed
    @ExceptionMetered
    @Path("/update")
    public ConfigDetails updateConfig(@Context HttpHeaders headers,
                                      @Valid final UpdateConfigRequest updateRequest) {
        final var updater = updaterResolver.get().resolve(headers);
        permissionValidatorSupplier.get().authorize(headers, updater, updateRequest);
        configDataValidatorSupplier.get().validate(updateRequest.getConfigKey(), updateRequest.getData());
        return ingestionService.updateConfiguration(updater, updateRequest);
    }

    @POST
    @Timed
    @ExceptionMetered
    @Path("/approve")
    public ConfigDetails approveConfig(@Context HttpHeaders headers,
                                       @Valid final ConfigKey configKey) {
        final var updater = updaterResolver.get().resolve(headers);
        permissionValidatorSupplier.get().authorizeApproval(headers, updater, configKey);
        return ingestionService.approveConfiguration(updater, configKey);
    }

    @POST
    @Timed
    @ExceptionMetered
    @Path("/reject")
    public ConfigDetails rejectConfig(@Context HttpHeaders headers,
                                      @Valid final ConfigKey configKey) {
        final var updater = updaterResolver.get().resolve(headers);
        permissionValidatorSupplier.get().authorizeRejection(headers, updater, configKey);
        return ingestionService.rejectConfiguration(updater, configKey);
    }

    @POST
    @Timed
    @ExceptionMetered
    @Path("/activate")
    public ConfigDetails activateConfig(@Context HttpHeaders headers,
                                        @Valid final ConfigKey configKey) {
        final var updater = updaterResolver.get().resolve(headers);
        permissionValidatorSupplier.get().authorizeActivation(headers, updater, configKey);
        return ingestionService.activateConfiguration(updater, configKey);
    }
}
