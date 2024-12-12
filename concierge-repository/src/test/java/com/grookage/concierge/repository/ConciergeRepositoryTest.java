package com.grookage.concierge.repository;

import com.grookage.concierge.models.ResourceHelper;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.repository.cache.CacheConfig;
import com.grookage.concierge.repository.stubs.TestableConciergeRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

class ConciergeRepositoryTest {

    @Test
    @SneakyThrows
    void testWithCacheEnabled() {
        final var cacheConfig = CacheConfig.builder()
                .enabled(true)
                .refreshCacheSeconds(1)
                .build();
        final var testableRepository = new TestableConciergeRepository(cacheConfig);
        final var configKey = ResourceHelper.getResource("configKey.json", ConfigKey.class);
        Assertions.assertFalse(testableRepository.getStoredRecord(configKey).isEmpty());
        Assertions.assertFalse(testableRepository.getStoredRecords().isEmpty());
        Assertions.assertFalse(testableRepository.getStoredRecords().isEmpty());
        Assertions.assertTrue(testableRepository.getActiveStoredRecords(Set.of()).isEmpty());
        Assertions.assertNotNull(testableRepository.getRecord(configKey).orElse(null));
        Assertions.assertFalse(testableRepository.getRecords("concierge", Set.of("testConfig")).isEmpty());
        Assertions.assertFalse(testableRepository.getRecords().isEmpty());
        Assertions.assertFalse(testableRepository.getRecords(Set.of("concierge")).isEmpty());

        testableRepository.resetConfigDetails();
        await().pollDelay(Duration.of(3, TimeUnit.SECONDS.toChronoUnit()))
                .untilAsserted(() -> Assertions.assertTrue(true));
        ;
        Assertions.assertTrue(testableRepository.getStoredRecord(configKey).isEmpty());
        Assertions.assertTrue(testableRepository.getStoredRecords().isEmpty());
        Assertions.assertNull(testableRepository.getRecord(configKey).orElse(null));
        Assertions.assertTrue(testableRepository.getRecords("concierge", Set.of("testConfig")).isEmpty());
        Assertions.assertTrue(testableRepository.getRecords().isEmpty());
        Assertions.assertTrue(testableRepository.getRecords(Set.of("concierge")).isEmpty());
    }

    @Test
    @SneakyThrows
    void testWithCacheDisabled() {
        final var testableRepository = new TestableConciergeRepository(null);
        final var configKey = ResourceHelper.getResource("configKey.json", ConfigKey.class);
        Assertions.assertFalse(testableRepository.getStoredRecords().isEmpty());
        Assertions.assertFalse(testableRepository.getRecords("concierge", Set.of("testConfig")).isEmpty());
        Assertions.assertFalse(testableRepository.getRecords(Set.of("concierge")).isEmpty());
        Assertions.assertFalse(testableRepository.getStoredRecord(configKey).isEmpty());
    }


}
