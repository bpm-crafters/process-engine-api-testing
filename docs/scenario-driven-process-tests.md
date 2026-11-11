---
title: Scenario-driven process tests
---

# Scenario-driven process tests

When a process starts, it does not simply return a result. It moves through a sequence of business
situations. A request may wait for an approval, then wait for payment, and finally complete. Along
the way, the process carries information — such as the request id, the approver's decision, or a payment
reference — that influences what happens next.

Internally, an engine represents this as work positioned at different points in the process and as
values associated with that work. A business stakeholder does not need to know those mechanics. The
important point is that the process has observable behaviour over time: after one event it should be
waiting for the next expected event, or it should have reached a defined outcome. That behaviour is
the process specification.

## Describe behaviour as scenarios

Behaviour-driven development (BDD) expresses such a specification as a scenario with a starting
context, an event, and an expected outcome: *given*, *when*, and *then*. It is a familiar approach
from tools such as Cucumber, JGiven, and SpecFlow. The language makes a test serve two purposes: it
checks the software and communicates the intended business behaviour.

Process Engine API Testing follows this approach for processes. A scenario moves along the process
control flow and describes what a business event causes next. It deliberately avoids treating a
process as one opaque end-to-end test.

## The ideal execution feedback

The ideal suite contains one test for every logical wait state or decision point. If the approval
path changes, tests for earlier states stay green. The test that expects the changed behaviour fails
at that expectation. Tests for later states cannot establish their starting situation and therefore
do not run beyond their prerequisite. This is helpful feedback: it shows which part of the process
is unchanged, where the behaviour diverged, and which later expectations depend on that point.

```text
Order received ──> Await approval ──> Await payment ──> Completed
       │                  │                   │
    test 1             test 2              test 3
```

## Why stages are needed

A scenario needs a driver that can create process situations, provide business events, and observe
the resulting state. In this context the driver talks to the process engine, so it is often called
an engine driver. Stages organise that driver into named parts of a scenario: one stage can arrange
or perform an action, while another expresses what should be observed afterwards.

The name comes from BDD: each stage is a meaningful part of the story being tested. Stages also hold
the scenario context, allowing one step to pass the current process, selected task, or relevant
business values to the next step without exposing engine details in the scenario.

## Reusable steps and domain stages

Separating the scenario from reusable steps is intentional. The scenario remains a concise business
story, while reusable steps contain the mechanics of reaching a state or sending an event. When the
process implementation changes, that work is updated once in a reusable step instead of throughout
the suite.

Domain stages take this further. Rather than reporting that a technical task with a particular key
was completed, they report that an order was approved or that payment was requested. The generated
BDD report is then understandable to business people: they can see their business being tested,
not the technical noise of the current implementation.

The [Process Testing API|Stages](testing-api.md) page explains how to build reusable stages and
contains the available process operations.
