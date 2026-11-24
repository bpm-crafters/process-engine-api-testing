# ADR 0003: Public JGiven DSL and Scenario State

## Status

Superseded by [ADR 0004](0004-independent-stage-roles-and-receiver-steps.md).

## Context

The public API targets JGiven-based customer process tests. The DSL should be readable in source, friendly to BDD chaining, and stable
across engine migration.

Customers may begin with engine-level primitives, but the preferred style is to extend the base stages into domain-specific business stages.

## Decision

Expose two public base stages:

- `AssertStage`
- `ActionStage extends AssertStage`

### Naming

- public stage methods use standard Java/Kotlin camelCase names
- JGiven `@Description` annotations provide sentence-like report output where useful
- `ActionStage` verbs use present/action phrasing
- `AssertStage` verbs use assertion/result phrasing

### Scenario state

The shared state model is intentionally small:

- one grouped `ProcessTestContext`
- one current `instanceId`
- one current `ProcessInformation`
- one current `TaskInformation`
- one current captured `Throwable`

### Query and selection model

- query API returns normalized `process-engine-api` types only
- selection is strict: the task must be present and unique
- restrictions are explicit
- predicate is optional
- filtering is always restrictions first, predicate second

### Public DSL shape

The public stages provide:

- basic generic verbs out of the box
- protected access to named sub-interfaces for advanced use
- protected convenience methods first, direct access second

Task subscriptions are not part of the customer-facing DSL.

Decision testing is not part of v1.

## Consequences

### Positive

- JGiven source stays highly readable
- customer business stages can build on stable primitives
- scenario state remains simple and deterministic

### Negative

- some adapter-specific convenience from old stages must be reintroduced through the new APIs
- some low-level edge cases are intentionally deferred to later iterations
