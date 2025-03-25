package com.grookage.concierge.core.engine;

import com.google.common.base.Preconditions;
import com.grookage.concierge.core.engine.processors.*;
import com.grookage.concierge.core.engine.resolver.AppendConfigResolver;
import com.grookage.concierge.core.engine.resolver.ConfigVersionManager;
import com.grookage.concierge.core.engine.resolver.DefaultAppendConfigResolver;
import com.grookage.concierge.core.managers.ProcessorFactory;
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
    private Supplier<ProcessorFactory> processorFactorySupplier;

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
        Preconditions.checkNotNull(manager, "Config Version Manager can't be null");
        this.configVersionManager = manager;
        return this;
    }

    public ConciergeHub withProcessorFactory(Supplier<ProcessorFactory> pfSupplier) {
        Preconditions.checkNotNull(pfSupplier, "Processor Factory can't be null");
        this.processorFactorySupplier = pfSupplier;
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
                return CreateConfigProcessor.builder()
                        .processorFactory(processorFactorySupplier)
                        .repositorySupplier(repositorySupplier)
                        .build();
            }

            @Override
            public ConciergeProcessor configAppend() {
                return AppendConfigProcessor.builder()
                        .repositorySupplier(repositorySupplier)
                        .appendConfigResolverSupplier(appendConfigResolverSupplier)
                        .processorFactory(processorFactorySupplier)
                        .build();
            }

            @Override
            public ConciergeProcessor configUpdate() {
                return UpdateConfigProcessor.builder()
                        .repositorySupplier(repositorySupplier)
                        .processorFactory(processorFactorySupplier)
                        .build();
            }

            @Override
            public ConciergeProcessor configApprove() {
                return ApproveConfigProcessor.builder()
                        .repositorySupplier(repositorySupplier)
                        .processorFactory(processorFactorySupplier)
                        .build();
            }

            @Override
            public ConciergeProcessor configReject() {
                return RejectConfigProcessor.builder()
                        .processorFactory(processorFactorySupplier)
                        .repositorySupplier(repositorySupplier)
                        .build();
            }

            @Override
            public ConciergeProcessor configActivate() {
                return ActivateConfigProcessor.builder()
                        .repositorySupplier(repositorySupplier)
                        .configVersionManager(configVersionManager)
                        .processorFactory(processorFactorySupplier)
                        .build();
            }
        }));
    }
}
