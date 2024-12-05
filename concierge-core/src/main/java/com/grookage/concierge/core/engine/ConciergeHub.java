package com.grookage.concierge.core.engine;

import com.google.common.base.Preconditions;
import com.grookage.concierge.core.engine.processors.*;
import com.grookage.concierge.core.managers.VersionGenerator;
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
    private Supplier<VersionGenerator> versionSupplier;

    private ConciergeHub() {

    }

    public static ConciergeHub of() {
        return new ConciergeHub();
    }

    public Optional<ConciergeProcessor> getProcessor(ConfigEvent configEvent) {
        return Optional.ofNullable(processors.get(configEvent));
    }

    public ConciergeHub withRepositoryResolver(Supplier<ConciergeRepository> repositorySupplier) {
        this.repositorySupplier = repositorySupplier;
        return this;
    }

    public ConciergeHub withVersionSupplier(Supplier<VersionGenerator> versionSupplier) {
        this.versionSupplier = versionSupplier;
        return this;
    }

    public ConciergeHub build() {
        Preconditions.checkNotNull(repositorySupplier, "Schema Repository can't be null");
        Preconditions.checkNotNull(versionSupplier, "Version ID Generator can't be null");
        Arrays.stream(ConfigEvent.values()).forEach(this::buildProcessor);
        return this;
    }

    private void buildProcessor(ConfigEvent configEvent) {
        processors.putIfAbsent(configEvent, configEvent.accept(new ConfigEventVisitor<>() {
            @Override
            public ConciergeProcessor configCreate() {
                return new CreateConfigProcessor(repositorySupplier, versionSupplier);
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
                return new ActivateConfigProcessor(repositorySupplier);
            }
        }));
    }
}