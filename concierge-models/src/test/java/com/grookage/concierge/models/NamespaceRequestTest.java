package com.grookage.concierge.models;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NamespaceRequestTest {

    @Test
    @SneakyThrows
    void testNamespaceRequest() {
        final var namespaceRequest = ResourceHelper
                .getResource("namespaceRequest.json", NamespaceRequest.class);
        Assertions.assertNotNull(namespaceRequest);
        Assertions.assertFalse(namespaceRequest.getNamespaces().isEmpty());
        Assertions.assertEquals(2, namespaceRequest.getNamespaces().size());
        Assertions.assertTrue(namespaceRequest.getNamespaces().contains("concierge"));
    }
}
