# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0]

- Release version for concierge. 

## [0.0.1-RC17]

- Bug fix in RC17, configKey needs to remain without configType. Just making it part of search

## [0.0.1-RC16] : [Broken]

- Made configType part of the referenceId as well. So in a set we can fetch by configTypes as required and ser/de-ser

## [0.0.1-RC15]

- Changed the elasticSearch schema structure

## [0.0.1-RC14]

- Release to fix RC13 maven push

## [0.0.1-RC13]

- Fixed the data saving format in Aerospike
- Fixed the rollOverAndUpdate
- Identified a weird client bug in Aerospike. If there are no records OR only one record Predicate.or/and don't work.
  They need special handling. And there's barely any documentation on the same

## [0.0.1-RC12]

- Introduced orgId and tenantId to configKey
- Cleaned up adhoc occurrences of namespace, configName et. al to a standard configKey

## [0.0.1-RC11]

- Moved to [Korg](https://github.com/grookage/korg) from Leia Refresher.

## [0.0.1-RC10]

- Added SearchRequest to filter on namespaces, configNames and configStates
- Minor code convention improvements
- Removed the latest from config, makes sense to have the actual version fetched, since version is now being supplied by
  the user - VersionIdGenerator has been removed.
- Removed the unnecessary configType interface and is now bound as a String

## [0.0.1-RC9]

- Moved CacheConfig and cache handling to ConfigService from Repository. This is now coupled with jersey resources
  sending an ignoreCache queryParam if real time fetch from datastores are required.
- Added an updateMessage on Config Creation, Update and on Append for configurations.

## [0.0.1-RC8]

- Introduced rollOverAndUpdate to configs. Only keeping one latest config by default, we can override to multiple active
  configs if need be.

## [0.0.1-RC7]

- Ingestion Resource http methods changed to reflect the actual behaviour. Append is a PATCH and Update is a PUT.

## [0.0.1-RC6]

- Formatting fixes

## [0.0.1-RC5]

- Introduced Config Data Validator

## [0.0.1-RC4]

- Optimizations
    - Added config appender during ingestion.
    - Introduced configType in processorKey during ProcessorSelection

## [0.0.1-RC3]

- Support for multiple versions of configs and support to fetch the latest version on demand

## [0.0.1-RC2]

- Added a preProcessor and post-processor for every config update, basis namespace, configName and configEvent
- Bug Fix : AS Repository wasn't saving namespace and config bins. Fixed it

## [0.0.1-RC1]

- A versioned config registry, to register various configs backed by a RBAC enabled maker-checker process
- A RESTful interface and a console to manage the said configs, to allow for easier integrations
- Listeners on state changes whenever the config state changes
- Extensible repository bindings for config store
- Default repository implementations for Aerospike and Elasticsearch
- A concierge client to fetch configs and keep it in-memory instead of fetching from source everytime.