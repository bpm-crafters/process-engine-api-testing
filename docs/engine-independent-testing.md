---
title: Engine-independent process testing
---

# Engine-independent process testing

A business process gives a business requirement an executable form: it coordinates decisions, work
performed by people or systems, and the resulting outcomes. The engine that runs it is the means of
execution, not the requirement itself. As performance, operational, ecosystem, or organisational
priorities evolve, a different engine can become the better means of meeting that same requirement.

Such a change should be possible without losing confidence in the business behaviour. Process Engine
API Testing describes and verifies that behaviour independently of a particular engine. It is a
regression and migration safety net: the tests express what the process must do, so a new runtime
and, where needed, a changed process-model implementation can be verified against the same
behavioural evidence.

## A typical migration scenario

1. Decide to move a process from its current engine to another supported engine.
2. Describe its important business behaviour with Process Engine API Testing while it still runs on
   the current engine.
3. Make the engine and model changes required by the migration.
4. Run the same scenarios on the target engine and use the results to verify the migration.

The resulting suite tells stakeholders which behaviour has been preserved and where an intended or
unintended difference remains. It also remains useful after the migration as a focused regression
suite for later process changes.

## Independent of the application implementation

The testing approach is not tied to how the surrounding application is implemented. An application
does not need to use Process Engine API or one of its adapters in production to gain value from the
testing library. The tests operate at the process boundary: they deploy a process, trigger business
events, and observe process behaviour through the testing integration for the chosen engine.

This separation is useful when modernising a legacy application, when process ownership is separate
from application ownership, or when the process models need an independent acceptance suite. Teams
can establish behavioural confidence before deciding whether to adopt the wider Process Engine API
in their application architecture.

The [scenario-driven process-tests approach](scenario-driven-process-tests.md) explains how to structure this
behaviour as readable process scenarios. Implementation and setup details belong in the
[process-testing quick start](testing-quickstart.md) and the reference material.
