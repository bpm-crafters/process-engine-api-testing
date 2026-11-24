# Constitution

## Status

Accepted as the governing design document for `process-engine-api-testing`.

## Purpose

This constitution defines the non-negotiable principles for the `process-engine-api-testing` library family. It is the highest-level design
authority for the project. More detailed behavior belongs in `spec.md`. Historical architectural commitments belong in ADRs.

## Principles

### 1. Customer Testing Only

This library exists for customer-facing process tests. It is not the testing technology for adapter-internal verification,
implementation-level adapter tests, or decision testing.

### 2. Engine-Agnostic Public Contract

Customer test source must depend only on engine-agnostic testing artifacts and `process-engine-api` abstractions. Public contracts must not
expose raw engine classes or engine-specific public stage types.

### 3. Reuse Existing `process-engine-api` Vocabulary

Wherever possible, the testing API reuses existing `process-engine-api` concepts and model types instead of creating a parallel domain
model. Existing types such as `TaskInformation` and `ProcessInformation` are preferred over new testing-only reference wrappers.

### 4. JGiven-First Customer Experience

The primary-intended usage is JGiven-based BDD process testing. Public stage APIs use standard Java/Kotlin camelCase naming and can use
JGiven `@Description` annotations for sentence-like report output. They are designed for extension through customer-defined business stages.
Actions and outcome assertions are distinct stage roles. Reusable scenario fragments are receiver-based `Step` functions, and individual
stage methods return their concrete JGiven stage for Java-friendly fluent chaining.

### 5. Domain-Level Tests Are Preferred

Customers may start with low-level process primitives, but the recommended style is to express business behavior by extending the common
base stages and translating business actions into `process-engine-api` primitives.

### 6. Ergonomic Defaults With Explicit Overrides

Payload defaults to an empty map, restrictions default to `CommonRestrictions.builder().build()`, and nullable selectors default to
`null`. Customer scenarios override those values explicitly when ambiguity or process data requires it. Engine choice remains an external
bootstrap concern, not scenario behavior.

### 7. Query and Assertion Separation

Query APIs are snapshot-only and return current facts. Assertion APIs are responsible for waiting, polling, eventual consistency, and
failure messages.

Payload snapshots are detached from engine state. Process and task payload mutation belongs to the payload API, and its effects are
verified through payload snapshots or public process behavior rather than raw-engine access.

Process-affecting actions synchronize their asynchronous side effects before their public operation
completes. Adapters own the engine-specific mechanism; customer tests synchronize on meaningful,
eventually consistent assertions and never use a generic continuation step or a polling library.

### 8. Restriction-First Semantics

When selecting tasks or asserting on runtime state, engine-supported restrictions are applied first. Optional predicates refine the already
restricted result set afterward.

### 9. Single Scenario Focus

The common scenario state model supports one current process instance and one current selected task at a time. More complex multi-instance
orchestration is not part of the generic v1 state model.

### 10. Adapter Pluggability by Named Initializer

Engine integration is provided by adapter-side initializers discovered externally. Initializer names align with existing adapter qualifier
vocabulary such as `c7remote`, `c7embedded`, and `c8`. The initializer-selection annotation may
also carry adapter-specific `key=value` configuration, which is passed to the selected initializer
without introducing engine types into the shared testing API.

Tests may also declare framework- or adapter-specific property providers. Providers receive the
explicit annotation configuration before the initializer runs and contribute additional properties.
Explicit test configuration overrides provider values. This permits optional integrations, such as
Spring configuration-data loading for an explicitly selected profile, without making Spring a
dependency of the shared contract.

### 11. Shared-Test Migration Proof

The architecture is only successful if the same shared customer scenario can be rerun unchanged across different engines by replacing only
runner setup, initializer selection, and engine-specific resources.

### 12. Executable Adapter Compatibility Contract

The library family provides a shared, engine-agnostic compatibility contract for adapter
repositories. Contract suites assert only customer-visible public behavior; adapters contribute
initializer selection, logical model mappings, and engine-specific BPMN resources without adding
scenario logic or exposing raw engine types to the contract.

## Project Boundaries

- Decision testing is out of scope for v1.
- Task subscription concepts are out of scope for the customer-facing DSL.
- Process stop/cancel is out of scope until it exists as a proper engine-agnostic runtime API.
- Direct task-id-driven public selection is out of scope for v1.
- Raw engine-object escape hatches are not allowed in the public API.

## Document Responsibilities

- `constitution.md`: principles and boundaries
- `spec.md`: detailed behavioral and structural specification
- `implementation-plan.md`: phased delivery and sequencing
- `adr/*.md`: irreversible or high-cost architectural choices
