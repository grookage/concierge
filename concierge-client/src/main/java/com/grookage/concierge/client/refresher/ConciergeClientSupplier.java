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

package com.grookage.concierge.client.refresher;

import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import com.grookage.concierge.models.MapperUtils;
import com.grookage.concierge.models.NamespaceRequest;
import com.grookage.concierge.models.ingestion.ConfigurationResponse;
import com.grookage.leia.provider.config.LeiaHttpConfiguration;
import com.grookage.leia.provider.suppliers.LeiaHttpSupplier;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings({"deprecation", "KotlinInternalInJava"})
@Getter
public class ConciergeClientSupplier extends LeiaHttpSupplier<List<ConfigurationResponse>> {

    private final Set<String> namespaces;
    private final Supplier<String> authHeaderSupplier;

    @Builder
    public ConciergeClientSupplier(LeiaHttpConfiguration httpConfiguration,
                                   Set<String> namespaces,
                                   Supplier<String> authHeaderSupplier) {
        super(httpConfiguration, ConciergeClientMarshallar.getInstance(), "getClientNamespaces");
        this.namespaces = namespaces;
        this.authHeaderSupplier = authHeaderSupplier;
    }

    @Override
    protected String url() {
        return "/v1/configs/details/active";
    }

    @Override
    @SneakyThrows
    protected Request getRequest(String url) {
        final var requestBody = RequestBody.create(
                okhttp3.MediaType.parse("application/json; charset=utf-8"),
                MapperUtils.mapper().writeValueAsString(NamespaceRequest.builder()
                        .namespaces(namespaces)
                        .build()));
        final var requestBuilder = new Request.Builder()
                .url(endPoint(url))
                .post(requestBody);
        final var suppliedHeader = authHeaderSupplier.get();
        if (!Strings.isNullOrEmpty(suppliedHeader)) {
            requestBuilder.addHeader(HttpHeaders.AUTHORIZATION, suppliedHeader);
        }
        return requestBuilder.build();
    }
}
