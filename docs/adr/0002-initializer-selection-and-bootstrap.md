# ADR 0002: Initializer Selection and Bootstrap

## Status

Accepted

## Context

Tests must remain engine-agnostic. Engine selection is infrastructure, not scenario behavior. Testing must not require Spring, but naming
should remain consistent with existing adapter qualifier vocabulary.

When several engine adapters are available, the selected initializer must be deterministic and diagnosable.

## Decision

Use Java `ServiceLoader` to discover `ProcessTestInitializer` implementations.

Each initializer exposes a stable qualifier via `qualifier()`. The qualifier vocabulary follows existing adapter naming, for example:

- `c7remote`
- `c7embedded`
- `c8`

Selection rules:

- if a qualifier annotation is present on the test class hierarchy, match by qualifier
- if no qualifier is present and exactly one initializer is discovered, auto-select it
- otherwise fail fast with a detailed diagnostic message

V1 explicit selection uses a qualifier-only annotation on the test class hierarchy.

Resolution scans:

- the concrete test class
- its superclasses

It does not scan interfaces or enclosing classes in v1.

Bootstrap is handled externally by test integration, not inside stages.

The produced `ProcessTestContext` is `AutoCloseable` and is closed by the integration layer after each test.

## Consequences

### Positive

- no Spring requirement for the public contract
- stable qualifier-based engine switching
- clear separation between test bootstrap and scenario behavior
- strong diagnostics when discovery is ambiguous or missing

### Negative

- adapter repositories must register services correctly
- runner modules may still need thin wrapper test classes to carry the qualifier annotation
