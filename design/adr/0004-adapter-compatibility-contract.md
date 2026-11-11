# ADR 0004: Adapter Compatibility Contract

## Status

Accepted

## Context

Adapter implementations need a repeatable, engine-agnostic way to prove that they implement the
customer-facing testing API consistently. A one-off example in each adapter repository would drift
over time and would not establish a reusable compatibility boundary.

The contract must test public behavior only. It cannot depend on raw engine types, adapter-private
test helpers, or a specific engine's BPMN resource format.

## Decision

Publish `process-engine-api-testing-contract` as a third artifact in the testing library family.
It contains reusable abstract JUnit/JGiven compatibility suites in its main source set so adapter
repositories can import it as a test dependency.

The contract artifact depends only on:

- `process-engine-api-testing-core`
- `process-engine-api-testing-jgiven`
- `process-engine-api`

The suites use only public testing contracts and public stages. The suite family is intended to
cover the generic behavior of deployment, starting processes, task selection and completion, path
assertions, completion and incident assertions, correlation, signals, exception capture, and
capability behavior. Its initial implementation covers the user-task normal path: deployment,
definition start, user-task selection and completion, ordered path assertions, and completion.

The contract also publishes C7-reference BPMN models as design material. Text annotations state
where a C7 mechanism is not portable, for example external-task topics, metadata keys, and
deterministic engine-clock control. They are not deployed by shared suites; adapters own their
executable fixture resources and map logical fixture values to their engine.

Each adapter supplies a thin concrete runner subclass that:

- selects its initializer;
- supplies its engine-specific BPMN resources;
- maps logical process, task, and element names required by the suite; and
- enables only the capability-specific suites it supports.

Engine-specific resources remain in adapter repositories. The common contract never imports an
engine class or assumes a raw-engine state representation.

Suites are organized by feature area and capability so an adapter can adopt them incrementally.
Passing a suite means compatibility with the asserted public behavior, not verification of adapter
internals or polling implementation details.

## Consequences

### Positive

- adapters share one executable definition of public testing behavior;
- regressions are caught consistently across adapter repositories;
- each adapter runner stays small and contains no duplicate scenario logic;
- support can grow incrementally without weakening already adopted compatibility coverage.

### Negative

- the library family gains a third published artifact;
- logical process-model mappings must be maintained by each adapter;
- contract changes require coordinated version alignment across adapter repositories.
- adding a runtime API to the testing context requires the corresponding adapter context wiring.
- adapters must implement the payload API with detached snapshots so compatibility suites can verify
  process-instance and task-local payload changes without raw engine access.
