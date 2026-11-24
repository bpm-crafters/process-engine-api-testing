# ADR 0001: Testing Library Topology

## Status

Accepted

## Context

Customer-facing process tests need an engine-agnostic home. Existing adapter-side testing packages are internal and engine-specific.

The new testing feature must expose one stable public contract while allowing adapter repositories to provide engine-specific
implementations.

## Decision

Create a new library family `process-engine-api-testing` with three artifacts:

- `process-engine-api-testing-core`
- `process-engine-api-testing-jgiven`
- `process-engine-api-testing-contract`

Keep adapter-specific implementations in the adapter repositories.

The common testing library owns:

- the initializer SPI
- grouped test context interfaces
- query and assert APIs
- capability and diagnostic contracts
- JGiven base stages and bootstrap integration
- reusable adapter compatibility suites

Its public packages make those responsibilities explicit:

- `.api` contains the engine-agnostic runtime, query, assertion, capability, diagnostic, and context contracts
- `.config` contains polling and initializer-selection configuration
- `.bootstrap` contains the initializer SPI and ServiceLoader resolution
- `.jgiven` contains the JGiven/JUnit integration

Adapter repositories own:

- concrete `ProcessTestInitializer` implementations
- concrete `ProcessTestContext` implementations
- `META-INF/services` registration
- engine-specific testing artifacts for customer use

The old adapter-side `adapter.*.testing` public stages are intended to be removed as a breaking change after the new library is introduced.

## Consequences

### Positive

- customer tests have one engine-agnostic dependency surface
- adapter-specific code stays close to adapter-specific runtime wiring
- the testing core can evolve independently from JGiven and adapter internals

### Negative

- cross-repo version alignment is required
- adapter repositories must publish new customer-facing testing artifacts
- migration requires a breaking change from old public testing stages
