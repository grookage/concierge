package com.grookage.concierge.core.engine;

import com.google.common.base.Preconditions;
import com.grookage.concierge.core.engine.processors.*;
import com.grookage.concierge.core.engine.resolver.AppendConfigResolver;
import com.grookage.concierge.core.engine.resolver.ConfigVersionManager;
import com.grookage.concierge.core.engine.resolver.DefaultAppendConfigResolver;
import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.config.ConfigEventVisitor;
import com.grookage.concierge.repository.ConciergeRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
public class ConciergeHub {

    private static final Map<ConfigEvent, ConciergeProcessor> processors = new ConcurrentHashMap<>();
    private Supplier<ConciergeRepository> repositorySupplier;
    private Supplier<AppendConfigResolver> appendConfigResolverSupplier = DefaultAppendConfigResolver::new;
    private ConfigVersionManager configVersionManager;

    private ConciergeHub() {

    }

    public static ConciergeHub of() {
        return new ConciergeHub();
    }

    public Optional<ConciergeProcessor> getProcessor(ConfigEvent configEvent) {
        return Optional.ofNullable(processors.get(configEvent));
    }

    public ConciergeHub withRepositoryResolver(Supplier<ConciergeRepository> repositorySupplier) {
        Preconditions.checkNotNull(repositorySupplier, "Schema Repository can't be null");
        this.repositorySupplier = repositorySupplier;
        return this;
    }

    public ConciergeHub withAppendConfigResolverSupplier(Supplier<AppendConfigResolver> appendConfigResolver) {
        Preconditions.checkNotNull(appendConfigResolver, "Append Config Resolver can't be null");
        this.appendConfigResolverSupplier = appendConfigResolver;
        return this;
    }

    public ConciergeHub withConfigVersionManager(ConfigVersionManager manager) {
        Preconditions.checkNotNull(configVersionManager, "Config Version Manager can't be null");
        this.configVersionManager = manager;
        return this;
    }

    public ConciergeHub build() {
        Arrays.stream(ConfigEvent.values()).forEach(this::buildProcessor);
        return this;
    }

    private void buildProcessor(ConfigEvent configEvent) {
        processors.putIfAbsent(configEvent, configEvent.accept(new ConfigEventVisitor<>() {
            @Override
            public ConciergeProcessor configCreate() {
                return new CreateConfigProcessor(repositorySupplier);
            }

            @Override
            public ConciergeProcessor configAppend() {
                return new AppendConfigProcessor(repositorySupplier, appendConfigResolverSupplier);
            }

            @Override
            public ConciergeProcessor configUpdate() {
                return new UpdateConfigProcessor(repositorySupplier);
            }

            @Override
            public ConciergeProcessor configApprove() {
                return new ApproveConfigProcessor(repositorySupplier);
            }

            @Override
            public ConciergeProcessor configReject() {
                return new RejectConfigProcessor(repositorySupplier);
            }

            @Override
            public ConciergeProcessor configActivate() {
                return new ActivateConfigProcessor(repositorySupplier, configVersionManager);
            }
        }));
    }
}
