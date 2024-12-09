package com.grookage.concierge.client;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.grookage.concierge.client.refresher.ConciergeClientRefresher;
import com.grookage.concierge.client.refresher.ConciergeClientSupplier;
import com.grookage.concierge.client.serde.SerDe;
import com.grookage.concierge.client.serde.SerDeFactory;
import com.grookage.concierge.models.NamespaceRequest;
import com.grookage.concierge.models.ResourceHelper;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.ingestion.ConfigurationResponse;
import com.grookage.leia.provider.config.LeiaHttpConfiguration;
import com.grookage.leia.provider.endpoint.EndPointScheme;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest
class ConciergeClientTest {

    @Test
    @SneakyThrows
    void testConciergeClient(WireMockRuntimeInfo wireMockRuntimeInfo) {
        final var namespaceRequest = NamespaceRequest.builder()
                .namespaces(Set.of("concierge"))
                .build();
        final var configResponse = ResourceHelper.getResource("configurationResponse.json",
                ConfigurationResponse.class);
        stubFor(post(urlEqualTo("/v1/configs/details/active"))
                .withRequestBody(binaryEqualTo(ResourceHelper.getObjectMapper().writeValueAsBytes(namespaceRequest)))
                .willReturn(aResponse()
                        .withBody(ResourceHelper.getObjectMapper().writeValueAsBytes(configResponse))
                        .withStatus(200)));
        val clientConfig = LeiaHttpConfiguration.builder()
                .host("127.0.0.1")
                .port(wireMockRuntimeInfo.getHttpPort())
                .scheme(EndPointScheme.HTTP)
                .connectTimeoutMs(30_000)
                .opTimeoutMs(30_000)
                .build();
        final var clientSupplier = new ConciergeClientSupplier(
                clientConfig,
                Set.of("concierge"),
                () -> "Auth"
        );
        final var clientRefresher = new ConciergeClientRefresher(
                clientSupplier,
                1,
                true
        );
        final var serDeFactory = new SerDeFactory() {
            @SuppressWarnings("unchecked")
            @Override
            public SerDe<CustomConvert> getSerDe(String configName) {
                return new SerDe<CustomConvert>() {
                    @Override
                    public CustomConvert convert(ConfigurationResponse configurationResponse) {
                        return CustomConvert.builder()
                                .namespace(configurationResponse.getConfigKey().getNamespace())
                                .configName(configurationResponse.getConfigKey().getConfigName())
                                .build();
                    }
                };
            }
        };
        final var conciergeClient = new ConciergeClient(serDeFactory, clientRefresher);
        final var configKey = ResourceHelper.getResource("configKey.json", ConfigKey.class);
        final var configuration = conciergeClient.getConfiguration(configKey).orElse(null);
        Assertions.assertNotNull(configuration);
        Assertions.assertTrue(configuration instanceof CustomConvert);
    }

    @Builder
    @Data
    static class CustomConvert {
        private String namespace;
        private String configName;
    }
}
