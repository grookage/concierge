package com.grookage.conceirge.dwserver;

import com.google.common.base.Preconditions;
import com.grookage.conceirge.dwserver.health.ConciergeHealthCheck;
import com.grookage.conceirge.dwserver.lifecycle.Lifecycle;
import com.grookage.conceirge.dwserver.permissions.PermissionValidator;
import com.grookage.conceirge.dwserver.resolvers.ConfigUpdaterResolver;
import com.grookage.conceirge.dwserver.resources.ConfigResource;
import com.grookage.conceirge.dwserver.resources.IngestionResource;
import com.grookage.concierge.core.engine.ConciergeHub;
import com.grookage.concierge.core.managers.ProcessorFactory;
import com.grookage.concierge.core.managers.VersionGenerator;
import com.grookage.concierge.core.services.ConfigService;
import com.grookage.concierge.core.services.IngestionService;
import com.grookage.concierge.core.services.impl.ConfigServiceImpl;
import com.grookage.concierge.core.services.impl.IngestionServiceImpl;
import com.grookage.concierge.models.ConfigUpdater;
import com.grookage.concierge.models.serde.SerDeFactory;
import com.grookage.concierge.repository.ConciergeRepository;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Supplier;

@NoArgsConstructor
@Getter
public abstract class ConciergeBundle<T extends Configuration, U extends ConfigUpdater> implements ConfiguredBundle<T> {

    private IngestionService<U> ingestionService;
    private Supplier<ConciergeRepository> repositorySupplier;
    private ConfigService configService;

    protected abstract Supplier<ConfigUpdaterResolver<U>> userResolver(T configuration);

    protected abstract Supplier<ConciergeRepository> getRepositorySupplier(T configuration);

    protected abstract Supplier<VersionGenerator> getVersionSupplier();

    protected abstract Supplier<PermissionValidator<U>> getPermissionResolver(T configuration);

    protected List<ConciergeHealthCheck> withHealthChecks(T configuration) {
        return List.of();
    }

    protected List<Lifecycle> withLifecycleManagers(T configuration) {
        return List.of();
    }

    protected Supplier<ProcessorFactory> withProcessorFactory(T configuration) {
        return null;
    }

    protected abstract SerDeFactory withSerDeFactory(T configuration);

    @Override
    public void run(T configuration, Environment environment) {
        final var userResolver = userResolver(configuration);
        Preconditions.checkNotNull(userResolver, "User Resolver can't be null");
        final var permissionResolver = getPermissionResolver(configuration);
        Preconditions.checkNotNull(permissionResolver, "Permission Resolver can't be null");

        this.repositorySupplier = getRepositorySupplier(configuration);
        Preconditions.checkNotNull(repositorySupplier, "Schema Repository Supplier can't be null");

        final var conciergeHub = ConciergeHub.of()
                .withRepositoryResolver(repositorySupplier)
                .withVersionSupplier(getVersionSupplier())
                .build();
        this.ingestionService = new IngestionServiceImpl<>(withProcessorFactory(configuration), conciergeHub);
        this.configService = new ConfigServiceImpl(repositorySupplier);
        withLifecycleManagers(configuration)
                .forEach(lifecycle -> environment.lifecycle().manage(new Managed() {
                    @Override
                    public void start() {
                        lifecycle.start();
                    }

                    @Override
                    public void stop() {
                        lifecycle.stop();
                    }
                }));

        final var serDeFactory = withSerDeFactory(configuration);
        Preconditions.checkNotNull(serDeFactory, "SerDe can't be null");

        withHealthChecks(configuration)
                .forEach(leiaHealthCheck -> environment.healthChecks().register(leiaHealthCheck.getName(), leiaHealthCheck));
        environment.jersey().register(new IngestionResource<>(ingestionService, userResolver, permissionResolver));
        environment.jersey().register(new ConfigResource(configService, serDeFactory));
        environment.jersey().register(new ConciergeExceptionMapper());
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        //NOOP. Nothing to do here.
    }
}
