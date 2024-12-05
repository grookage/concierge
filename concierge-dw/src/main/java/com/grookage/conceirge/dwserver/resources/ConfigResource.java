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
import com.grookage.concierge.core.services.ConfigService;
import com.grookage.concierge.models.MapperUtils;
import com.grookage.concierge.models.NamespaceRequest;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.ingestion.ConfigurationResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.security.PermitAll;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Getter
@Setter
@Path("/v1/configs")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@AllArgsConstructor
@PermitAll
public class ConfigResource {

    private final ConfigService configService;

    @SneakyThrows
    private List<ConfigurationResponse> getConfigurationResponses(List<ConfigDetails> details) {
        final var responses = new ArrayList<ConfigurationResponse>();
        for (ConfigDetails response : details) {
            responses.add(ConfigurationResponse.builder()
                    .configKey(response.getConfigKey())
                    .description(response.getDescription())
                    .configState(response.getConfigState())
                    .configHistories(response.getConfigHistories())
                    .data(MapperUtils.mapper().readTree(new String(response.getData())))
                    .build());
        }
        return responses;
    }

    @POST
    @Timed
    @ExceptionMetered
    @Path("/summary")
    public List<ConfigurationResponse> getSchemaDetails(@Valid final ConfigKey configKey) {
        final var config = configService.getConfig(configKey).orElse(null);
        return getConfigurationResponses(null == config ? List.of() : List.of(config));
    }

    @POST
    @Timed
    @ExceptionMetered
    @Path("/details/active")
    public List<ConfigurationResponse> getActiveConfigs(@Valid final NamespaceRequest namespaceRequest) {
        return getConfigurationResponses(configService.getActiveConfigs(namespaceRequest.getNamespaces()));
    }

    @POST
    @Timed
    @ExceptionMetered
    @Path("/details")
    public List<ConfigurationResponse> getAllConfigDetails(@Valid final NamespaceRequest namespaceRequest) {
        return getConfigurationResponses(configService.getConfigs(namespaceRequest.getNamespaces()));
    }

    @GET
    @Timed
    @ExceptionMetered
    @Path("/all")
    public List<ConfigurationResponse> getAllSchemaDetails() {
        return getConfigurationResponses(configService.getConfigs());
    }

}
