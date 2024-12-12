# concierge

> "When you gaze long enough into the abyss, the abyss also gazes into you."
> - Friedrich Nietzsche, The Abyss

Concierge is an RBAC enabled, metadata store aimed at providing versioned,
configuration snapshots, with various state transitions built in.

- A versioned config registry, to register various configs bound by a maker-checker process, with customizable RBAC.
- A RESTful interface and a console to manage the said configs, to allow for easier integrations
- A client for these configs to be fetched runtime and kept in memory for clients to use the config repository
  seamlessly

## Maven Dependency

- The bom is available at

```
<dependency>
    <groupId>com.grookage.concierge</groupId>
    <artifactId>concierge-bom</artifactId>
    <versio>latest</version>
</dependency>
```

## Build Instructions

- Clone the source:

      git clone github.com/grookage/leia

- Build

      mvn install

## Getting Started

### Using the config bundle, with your custom repository as part of your server

```
@NoArgsConstructor
@Getter
public abstract class ConciergeCustomBundle<T extends Configuration, U extends ConfigUpdater> extends ConciergeBundle<T, U> {

    @Override
    protected Supplier<ConciergeRepository> getRepositorySupplier(T configuration) {
        return () -> <your_custom_repo>;
    }

    @Override
    protected List<ConciergeHealthCheck> withHealthChecks(T configuration) {
        return List.of(new CustomHealthCheck(elasticConfig, elasticsearchClient));
    }

    @Override
    public void run(T configuration, Environment environment) {
        //Any of your initializtion code.
        super.run(configuration, environment);
    }
    
    protected Supplier<VersionGenerator> getVersionSupplier() {
        //Your custom versionId Generator
    }

    protected abstract Supplier<PermissionValidator<U>> getPermissionResolver(T configuration) {
        //Your custom Permission Validator
    }

}
```

The above bundle provides

- An Ingestion resource - To help with creating and editing the configs, backed by the `PermissionValidator`
- A Config Resource - To help with getting the config details, backed by the `PermissionValidator`

Additionally, Concierge comes with default implementations for ElasticSaerch and Aerospike

### Using the elastic bundle in your dropwizard project

```
      new ConciergeElasticBundle<AppConfiguration>() {

            @Override
            protected CacheConfig getCacheConfig(Configuration configuration) {
                return null;
            }

            @Override
            protected ElasticConfig getElasticConfig(Configuration configuration) {
                return null;
            }

            @Override
            protected Supplier<ConfigUpdaterResolver> userResolver(Configuration configuration) {
                return null;
            }

            @Override
            protected Supplier<VersionGenerator> getVersionSupplier() {
                return null;
            }

            @Override
            protected Supplier<PermissionValidator> getPermissionResolver(Configuration configuration) {
                return null;
            }
        }
```

### Using the Aerospike bundle in your dropwizard project

```
      new ConciergeAerospikeBundle<AppConfiguration>() {

            @Override
            protected CacheConfig getCacheConfig(Configuration configuration) {
                return null;
            }

            @Override
            protected AerospikeConfig getElasticConfig(Configuration configuration) {
                return null;
            }

            @Override
            protected Supplier<ConfigUpdaterResolver> userResolver(Configuration configuration) {
                return null;
            }

            @Override
            protected Supplier<VersionGenerator> getVersionSupplier() {
                return null;
            }

            @Override
            protected Supplier<PermissionValidator> getPermissionResolver(Configuration configuration) {
                return null;
            }
        }
```

Concierge also comes with a client, that you can provide to the calling services or wrap it up in your client offering
and provide it further,
to periodically refresh and keep the configs in-memory instead of fetching from source all the time.

### To use the client bundle

```
       new ConciergeClientBundle<AppConfiguration>() {
            @Override
            protected Set<String> getNamespaces(AppConfiguration configuration) {
                // Namespaces against which client wants to fetch configs
            }

            @Override
            protected LeiaHttpConfiguration getHttpConfiguration(AppConfiguration configuration) {
                // Http Configuration where concierge server resides. Usually supplied by the config manager
            }

            @Override
            protected SerDeFactory getSerDeFactory(AppConfiguration configuration) {
                // A custom SerDe Factory if you have different serializations for config. Again can be extended as required.
            }
        }

```

LICENSE
-------

Copyright 2024 Koushik R <rkoushik.14@gmail.com>.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.