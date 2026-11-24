# ADR 0005: Asynchronous Action Synchronization

## Status

Accepted

## Context

Every process-affecting customer action can cause asynchronous continuation. This includes process
starts, task completion, correlation, signals, time advancement, and payload changes. A successful
engine command commonly means only that the command was accepted; an immediate process query can
therefore observe stale state, a transient async boundary, or no active process after completion.

A generic `processContinues()` operation was considered as a customer-facing synchronization step.
It cannot provide a portable completion guarantee because a process may legitimately become idle at
a user task, external task, timer, message catch, or scheduled boundary. It also mixes two
different adapter concerns: deterministically driving deferred work in a controllable engine and
waiting for an independently running executor.

## Decision

The public testing API has no process-continuation capability and no generic
`processContinues()` stage method.

Adapters must make every process-affecting asynchronous operation exposed through
`ProcessTestRuntimeApis` complete only after the operation's observable engine side effects are
available through the context query and assertion APIs. Completion must not mean command
acknowledgement alone. Synchronous payload mutations follow the same rule.

The adapter chooses its implementation strategy:

- in a controlled embedded test environment, it may find and release the concrete deferred job or
  equivalent unit of work;
- in a live or remote environment, it may await the normal executor and the relevant observable
  state.

The strategy is adapter-private. It must use the context's polling configuration and preserve useful
failure diagnostics, including the operation, process identity when known, last observed state, and
the final unsuccessful condition.

Customer scenarios synchronize only on a specific observable outcome through the assertion API,
for example a selected task, active BPMN element, completed process, incident, or history/path
assertion. Assertion APIs own any further eventual polling. Customer code must not use Awaitility
or a generic polling loop.

## Consequences

### Positive

- the same scenario has stable semantics across embedded and remote adapters;
- command acknowledgement is never mistaken for process progression;
- waiting, timeout policy, and diagnostics stay inside the testing integration;
- customer scenarios state the business-relevant result rather than an engine execution detail.

### Negative

- adapter implementations must define and test synchronization for every asynchronous operation;
- an adapter cannot expose a vague "continue until idle" behavior as a substitute for a defined
  observable outcome;
- controlled engines retain useful internal job-driving code, but it is not a common public DSL
  feature.
