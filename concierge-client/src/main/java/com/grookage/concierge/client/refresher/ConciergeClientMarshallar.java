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

import com.fasterxml.jackson.core.type.TypeReference;
import com.grookage.concierge.models.MapperUtils;
import com.grookage.concierge.models.ingestion.ConfigurationResponse;
import com.grookage.korg.marshal.Marshaller;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.List;

@NoArgsConstructor
public class ConciergeClientMarshallar implements Marshaller<List<ConfigurationResponse>> {
    public static ConciergeClientMarshallar getInstance() {
        return new ConciergeClientMarshallar();
    }

    @Override
    @SneakyThrows
    public List<ConfigurationResponse> marshall(byte[] body) {
        return MapperUtils.mapper().readValue(body, new TypeReference<>() {
        });
    }
}
