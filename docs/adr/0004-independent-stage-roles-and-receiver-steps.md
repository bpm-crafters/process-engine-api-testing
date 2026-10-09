# ADR 0004: Independent Stage Roles and Receiver Steps

## Status

Accepted

## Context

`ActionStage` previously extended `AssertStage` to share scenario state and allow setup to wait
for process readiness. This made every outcome assertion callable from an action stage, despite
actions and assertions being distinct customer-facing roles. Kotlin JGiven scenarios are naturally
receiver blocks, while Java/JGiven consumers require fluent concrete-stage returns.

## Decision

Introduce `ProcessStage` as the shared base for scenario state, protected API access, task
selection, and readiness synchronization. `ActionStage` and `AssertStage` both extend it and do
not extend one another.

All public stage operations return their concrete stage via `self()` for Java/JGiven chaining. A
reusable Kotlin scenario fragment is a receiver function: `Step<STAGE> = STAGE.() -> Unit`,
created with `step { ... }` and supplied to the Kotlin `given`, `whenever`, or `then` helpers.

## Consequences

Java consumers retain fluent JGiven stages, Kotlin consumers can reuse named receiver fragments,
and outcome assertions are no longer exposed as action-stage methods. Operational readiness and
strict task selection remain available to either stage because they prepare a subsequent step.
