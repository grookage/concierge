# Changelog

All notable changes to this project will be documented in this file.

## [0.0.1-RC1]

- A versioned config registry, to register various configs backed by a RBAC enabled maker-checker process
- A RESTful interface and a console to manage the said configs, to allow for easier integrations
- Listeners on state changes whenever the config state changes
- Extensible repository bindings for config store
- Default repository implementations for Aerospike and Elasticsearch
- A concierge client to fetch configs and keep it in-memory instead of fetching from source everytime.