package com.grookage.conceirge.dwserver;

import com.google.common.base.Preconditions;
import com.grookage.conceirge.dwserver.health.ConciergeHealthCheck;
import com.grookage.conceirge.dwserver.lifecycle.Lifecycle;
import com.grookage.conceirge.dwserver.permissions.PermissionValidator;
import com.grookage.conceirge.dwserver.resolvers.ConfigUpdaterResolver;
import com.grookage.conceirge.dwserver.resources.ConfigResource;
import com.grookage.conceirge.dwserver.resources.IngestionResource;
import com.grookage.concierge.core.cache.CacheConfig;
import com.grookage.concierge.core.engine.ConciergeHub;
import com.grookage.concierge.core.engine.resolver.AppendConfigResolver;
import com.grookage.concierge.core.engine.resolver.ConfigVersionManager;
import com.grookage.concierge.core.engine.resolver.DefaultAppendConfigResolver;
import com.grookage.concierge.core.engine.resolver.DefaultConfigVersionManager;
import com.grookage.concierge.core.engine.validator.ConfigDataValidator;
import com.grookage.concierge.core.managers.ProcessorFactory;
import com.grookage.concierge.core.managers.VersionGenerator;
import com.grookage.concierge.core.services.ConfigService;
import com.grookage.concierge.core.services.IngestionService;
import com.grookage.concierge.core.services.impl.ConfigServiceImpl;
import com.grookage.concierge.core.services.impl.IngestionServiceImpl;
import com.grookage.concierge.models.ConfigUpdater;
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

    protected abstract CacheConfig getCacheConfig(T configuration);

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

    protected Supplier<AppendConfigResolver> getAppendConfigResolver(T configuration) {
        return DefaultAppendConfigResolver::new;
    }

    protected ConfigVersionManager getConfigVersionManageR(T configuration) {
        return new DefaultConfigVersionManager();

    }

    protected abstract Supplier<ConfigDataValidator> getConfigDataValidator(T configuration);

    @Override
    public void run(T configuration, Environment environment) {
        final var userResolver = userResolver(configuration);
        Preconditions.checkNotNull(userResolver, "User Resolver can't be null");
        final var permissionResolver = getPermissionResolver(configuration);
        Preconditions.checkNotNull(permissionResolver, "Permission Resolver can't be null");

        this.repositorySupplier = getRepositorySupplier(configuration);
        Preconditions.checkNotNull(repositorySupplier, "Schema Repository Supplier can't be null");

        final var configDataValidator = getConfigDataValidator(configuration);
        Preconditions.checkNotNull(configDataValidator, "Config Data Resolver can't be null");

        final var configVersionManager = getConfigVersionManageR(configuration);
        Preconditions.checkNotNull(configVersionManager, "Config Version Manager can't be null");

        final var cacheConfig = getCacheConfig(configuration);

        final var conciergeHub = ConciergeHub.of()
                .withRepositoryResolver(repositorySupplier)
                .withVersionSupplier(getVersionSupplier())
                .withAppendConfigResolverSupplier(getAppendConfigResolver(configuration))
                .withConfigVersionManager(configVersionManager)
                .build();
        this.ingestionService = new IngestionServiceImpl<>(withProcessorFactory(configuration), conciergeHub);
        this.configService = new ConfigServiceImpl(repositorySupplier, cacheConfig);
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

        withHealthChecks(configuration)
                .forEach(leiaHealthCheck -> environment.healthChecks().register(leiaHealthCheck.getName(), leiaHealthCheck));
        environment.jersey().register(new IngestionResource<>(ingestionService, userResolver, permissionResolver, configDataValidator));
        environment.jersey().register(new ConfigResource(configService));
        environment.jersey().register(new ConciergeExceptionMapper());
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        //NOOP. Nothing to do here.
    }
}
