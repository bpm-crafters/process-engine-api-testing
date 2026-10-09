# Implementation Plan

## Role of This Document

This document is the delivery sequence for the architecture defined in `constitution.md`, `spec.md`, and the ADR set. It does not redefine product requirements.

## Phase 1: Documentation Authority

Establish the design documents as the authoritative source of truth:

- `AGENTS.md`
- `docs/constitution.md`
- `docs/spec.md`
- `docs/adr/*.md`
- `docs/implementation-plan.md`

### Exit criteria

- the design is captured consistently in SDD-style documents
- previous ad hoc product docs are removed or folded into the new structure

## Phase 2: Common Core Contracts

Implement `process-engine-api-testing-core`:

- initializer SPI
- grouped context contracts
- query API
- assert API
- capabilities and diagnostics contracts
- qualifier annotation
- resolver

### Exit criteria

- no public engine runtime classes leak into core contracts
- bootstrap and capability boundaries are explicit

## Phase 3: JGiven Integration

Implement `process-engine-api-testing-jgiven`:

- `AssertStage`
- `ActionStage`
- JUnit/JGiven bootstrap integration
- scenario-state model

### Exit criteria

- the stage API matches the naming and state rules in `spec.md`
- the stage API depends only on core contracts and `process-engine-api`

## Phase 4: Adapter-Side Customer Testing Artifacts

Add new customer-facing testing artifacts in adapter repositories:

- `c7remote`
- `c8`

Each adapter-side artifact provides:

- initializer implementation
- grouped context implementation
- query implementation
- assert implementation
- capabilities implementation
- `META-INF/services` registration

### Exit criteria

- adapters plug into the common SPI without public engine leakage into shared customer code

## Phase 5: Published Adapter Compatibility Contract

Create `process-engine-api-testing-contract` as a published test-support artifact containing:

- reusable abstract JUnit/JGiven compatibility suites
- common customer stages where required by those suites
- logical process-model mappings and fixture contracts
- feature- and capability-scoped suite organization

The initial delivery is `UserTaskProcessCompatibilitySuite`, which proves the deployment, start,
selection, completion, ordered-path, and completion assertions for a process with one user task.

The next implemented suite set covers user-task assignment refresh, intermediate message correlation,
signal delivery, clock-driven timers, external-task worker completion, message starts, and
element-entry starts. C7-reference models for these suites live in
`contract/src/main/resources/reference/c7`; adapters copy or translate them into adapter-owned
test resources.

This contract artifact depends only on:

- `process-engine-api-testing-core`
- `process-engine-api-testing-jgiven`

### Exit criteria

- contract suites assert only public testing behavior
- contract source imports no raw engine or adapter-private test type
- adapter repositories can consume the artifact as a test dependency

## Phase 6: Thin Adapter Compatibility Runners

Create thin runner modules for:

- `c7remote`
- `c8`

Runner modules provide:

- engine-specific artifact selection
- setup and wiring
- engine-specific BPMN resources and logical process-model mappings
- concrete contract subclasses with initializer selection

### Exit criteria

- runner-local test subclasses contain no duplicated scenario logic
- shared contract scenarios remain unchanged across adapters

## Phase 7: First Proof Slice

Implement the first mandatory unchanged-test proof:

1. explicit deployment
2. process start
3. external task selection and completion
4. user task selection and completion
5. message correlation after task completion
6. process path assertion
7. process finished assertion

The same scenario must run unchanged against:

- `c7remote`
- `c8`

## Deferred Work

After the first proof slice:

- time-control-focused acceptance scenario
- additional assertion variants
- richer convenience overloads if needed
- decision-testing support with a non-JGiven approach
- removal of old adapter-side public testing stages
