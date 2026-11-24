# ADR 0006: Declarative Process-Test Property Providers

## Status

Accepted

## Context

An adapter may need configuration before it can create its process-test context. In particular, a
Spring Boot adapter may need an active profile and the properties loaded for that profile. Passing
only opaque `key=value` values forces this loading into imperative adapter code and makes the
configuration sources invisible at the test declaration.

The shared testing artifacts must not acquire a Spring dependency, and a provider cannot depend on
the already-created application context when it contributes properties used to create that context.

## Decision

`@UseProcessTestInitializer` accepts `propertyProviders`, an ordered list of
`ProcessTestPropertyProvider` classes. Each provider has a public no-argument constructor and is
called before the selected initializer. It receives the concrete test class and explicitly declared
annotation configuration, then returns a map of properties.

The resolver merges providers in declaration order. A later provider overrides an earlier one; an
explicit annotation property overrides all provider values. The resolved map is supplied in
`ProcessTestInitializationRequest`.

Spring-aware providers live in an optional Spring integration or adapter artifact. Such a provider
uses explicit bootstrap inputs such as `spring.profiles.active` to load Spring configuration data
before application-context refresh. It does not read an already-running application context.

## Consequences

### Positive

- test-visible configuration sources and predictable override semantics;
- no Spring dependency in `core` or the public generic initializer contract;
- adapters can share a reusable Spring configuration-data provider;
- explicit test-only overrides remain possible.

### Negative

- provider implementations need a public no-argument constructor;
- providers run during test bootstrap and should avoid hidden mutable global state;
- loading semantics for a framework remain owned and documented by that framework-specific provider.
