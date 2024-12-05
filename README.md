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
