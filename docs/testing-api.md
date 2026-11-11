---
title: Process testing API
---

# Process testing API

`process-engine-api-testing` provides an adapter-neutral API for testing BPMN processes. Your
test describes the process behaviour; the selected adapter testing library supplies the engine
connection and its `ProcessTestInitializer`. This keeps the scenario free of engine runtime APIs
and lets it be reused when the underlying engine changes.

The primary API is the JGiven DSL in `process-engine-api-testing-jgiven`. Extend
`ProcessScenarioTest` with your action and assertion stages, and select an initializer when more
than one is available on the test class path:

=== "Kotlin"

    ```kotlin
    @UseProcessTestInitializer(MyAdapterProcessTestInitializer.QUALIFIER)
    class OrderProcessTest : ProcessScenarioTest<OrderActions, OrderAssertions>() {
      @Test fun `approved order completes`() {
        given {
          processIsDeployed("bpmn/order-approval.bpmn")
          processIsStartedByDefinition("order-approval")
        }
        whenever {
          userTaskIsCompleted("approve-order", mapOf("approved" to true))
        }
        then {
          processHasPassedInOrder("start", "approve-order", "end")
          processIsFinished()
        }
      }
    }
    open class OrderActions : ActionStage<OrderActions>()
    open class OrderAssertions : AssertStage<OrderAssertions>()
    ```

=== "Java"

    ```java
    @UseProcessTestInitializer(MyAdapterProcessTestInitializer.QUALIFIER)
    class OrderProcessTest extends ProcessScenarioTest<OrderActions, OrderAssertions> {
      @Test void approvedOrderCompletes() {
        given()
          .processIsDeployed("bpmn/order-approval.bpmn")
          .processIsStartedByDefinition("order-approval");
        when()
          .userTaskIsCompleted("approve-order", Map.of("approved", true));
        then()
          .processHasPassedInOrder("start", "approve-order", "end")
          .processIsFinished();
      }
    }
    class OrderActions extends ActionStage<OrderActions> {}
    class OrderAssertions extends AssertStage<OrderAssertions> {}
    ```

The initializer is discovered through `ServiceLoader`. If exactly one initializer is available,
`@UseProcessTestInitializer` is optional. An adapter can also define initializer configuration or
property providers through that annotation.

## Scenario methods

Each scenario represents one current process instance. Starting a process sets that instance;
selecting or waiting for a task sets the current task. Methods that operate on the current process
or task fail with a clear error when the required state has not been established.

### Deployment and process start

Use these `ActionStage` methods to prepare a process and make an instance current:

| Method | Purpose |
| --- | --- |
| `processIsDeployed(classpathResource)` | Deploy one BPMN resource from the test class path. |
| `processIsDeployed(deployBundleCommand)` | Deploy a bundle of resources using the Process Engine API command. |
| `processIsStartedByDefinition(definitionKey, payload, restrictions)` | Start a definition by key. |
| `processIsStartedByDefinitionAtElement(definitionKey, elementId, payload, restrictions)` | Start a definition at a specific BPMN element. |
| `processIsStartedByMessage(messageName, payload, restrictions)` | Start a process through a message start event. |

Payload and restrictions are optional for the start methods. Restrictions use the common
Process Engine API restriction model and are passed to the adapter before any optional filtering.

### Messages, signals, and time

Use `messageIsCorrelated(messageName, correlation, payload, restrictions)` to deliver a BPMN
message to a running process, and `signalIsSent(signalName, payload, restrictions)` to send a
signal. Both refresh the current process snapshot after delivery.

`timePasses(duration)` advances the engine clock for timer-based scenarios. It is available only
when the selected adapter reports `supportsTimeTravel()`. When it is supported, the method returns
after the work made due by that elapsed time can be observed through the testing API.

### Task selection and actions

Task-selection methods wait until exactly one matching task is available and make it the current
task:

| Method | Purpose |
| --- | --- |
| `processWaitsInUserTask(taskDescriptionKey, restrictions, predicate)` | Wait for a user task in the current process and select it. |
| `processWaitsInElement(elementId, restrictions)` | Wait for the current process to have an active token at a BPMN element. |
| `userTaskIsSelected(taskDescriptionKey, restrictions, predicate)` | Select exactly one available user task. |
| `externalTaskIsSelected(taskDescriptionKey, restrictions, predicate)` | Select exactly one available external task. |

`taskDescriptionKey` is optional. A `predicate` is an optional `Predicate<TaskInformation>` that
is applied after restrictions; it is useful when a key alone does not identify a task. An empty or
ambiguous match is an assertion failure, rather than an arbitrary task choice.

Once a task is selected, use one of these action methods:

| Method | Purpose |
| --- | --- |
| `userTaskIsCompleted(payload)` | Complete the selected user task. |
| `userTaskIsCompleted(taskDescriptionKey, payload, restrictions, predicate)` | Select and complete a user task in one step. |
| `selectedUserTaskIsAssignedTo(assignee)` | Change the selected user task's assignee. |
| `externalTaskIsCompleted(payload)` | Complete the selected external task. |
| `externalTaskIsCompleted(taskDescriptionKey, payload, restrictions, predicate)` | Select and complete an external task in one step. |
| `externalTaskHasFailed(reason, retries)` | Fail the selected external task with the remaining retry count. |

### Payload methods

`currentProcessPayloadIsUpdated(payload)` updates the current process payload.
`selectedUserTaskPayloadIsUpdated(payload)` updates local payload values of the selected user
task. To assert values, use `processPayloadContains(expectedPayload)` or
`selectedTaskPayloadContains(expectedPayload)`. Both assertions require all supplied entries to
match; they do not require the payload to contain only those entries.

## Outcome assertions

`AssertStage` owns outcome assertions. The adapter performs its configured polling and supplies
diagnostic failures, so tests should call these methods instead of implementing their own waits.

| Method | Purpose |
| --- | --- |
| `processHasPassed(elementIds)` | Verify that the current process passed every listed BPMN element. |
| `processHasPassedInOrder(elementIds)` | Verify that the listed BPMN elements were passed in the supplied order. |
| `processHasNotPassed(elementIds)` | Verify that none of the listed BPMN elements was passed. |
| `processIsFinished(restrictions)` | Wait until the current process completes. |
| `processHasIncidents(restrictions)` | Wait until the current process has at least one incident. |
| `selectedTaskHasMetadata(key, value)` | Verify metadata on the selected task. |

All process-path methods also accept restrictions as their first argument. BPMN element IDs are
used deliberately: they make the assertions independent of an engine's internal execution IDs.

## Extending the DSL

Create non-final subclasses of `ActionStage` and `AssertStage` for business vocabulary. JGiven
creates reporting subclasses at runtime, so Kotlin stages must be `open`. Generic methods remain
available in those stages, while domain methods can translate business language into the same
adapter-neutral operations.

For advanced cases, stages provide protected access to the grouped `ProcessTestContext`: runtime
Process Engine API clients, snapshot queries, assertions, payload access, capabilities, and
diagnostics. Query APIs return snapshots only; waiting and assertion semantics belong to the
assertion API.

## Verifying adapter support

Adapter maintainers can use `process-engine-api-testing-contract` as a test dependency. It
contains abstract compatibility suites for user-task paths, user-task assignment, messages and
signals, timers, external tasks, and alternate starts. A concrete adapter test selects its
initializer and supplies a logical fixture mapping to its own BPMN resources. This verifies the
adapter integration without exposing engine types in the shared contract.
