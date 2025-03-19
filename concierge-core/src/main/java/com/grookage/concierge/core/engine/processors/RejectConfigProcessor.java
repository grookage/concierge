package com.grookage.concierge.core.engine.processors;

import com.grookage.concierge.core.engine.ConciergeContext;
import com.grookage.concierge.core.engine.ConciergeProcessor;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.config.ConfigKey;
import com.grookage.concierge.models.config.ConfigState;
import com.grookage.concierge.models.exception.ConciergeCoreErrorCode;
import com.grookage.concierge.models.exception.ConciergeException;
import com.grookage.concierge.repository.ConciergeRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.function.Supplier;

@SuperBuilder
@Slf4j
@Getter
public class RejectConfigProcessor extends ConciergeProcessor {

    private static final Set<ConfigState> ACCEPTABLE_STATES = Set.of(ConfigState.CREATED,
            ConfigState.APPROVED, ConfigState.ACTIVATED);

    private final Supplier<ConciergeRepository> repositorySupplier;

    @Override
    public ConfigEvent name() {
        return ConfigEvent.REJECT_CONFIG;
    }

    @Override
    @SneakyThrows
    public void process(ConciergeContext context) {
        final var configKey = context.getContext(ConfigKey.class)
                .orElseThrow((Supplier<Throwable>) () -> ConciergeException.error(ConciergeCoreErrorCode.VALUE_NOT_FOUND));
        final var storedConfig = getRepositorySupplier().get().getStoredRecord(configKey).orElse(null);
        if (null == storedConfig || !ACCEPTABLE_STATES.contains(storedConfig.getConfigState())) {
            log.error("There are no stored configs present with namespace {}, version {} and configName {}. Please try updating them instead",
                    configKey.getNamespace(),
                    configKey.getVersion(),
                    configKey.getConfigName());
            throw ConciergeException.error(ConciergeCoreErrorCode.NO_CONFIG_FOUND);
        }
        addHistory(context, storedConfig, null);
        storedConfig.setConfigState(ConfigState.REJECTED);
        getRepositorySupplier().get().update(storedConfig);
        context.addContext(ConfigDetails.class.getSimpleName(), storedConfig);
    }
}
