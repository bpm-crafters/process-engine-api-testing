# Process Engine API Testing

[![stable](https://img.shields.io/badge/lifecycle-STABLE-green.svg)](https://github.com/holisticon#open-source-lifecycle)
[![Development branches](https://github.com/bpm-crafters/process-engine-api-testing/actions/workflows/development.yml/badge.svg)](https://github.com/bpm-crafters/process-engine-api-testing/actions/workflows/development.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/dev.bpm-crafters.process-engine-api-testing/process-engine-api-testing)](https://maven-badges.herokuapp.com/maven-central/dev.bpm-crafters.process-engine-api-testing/process-engine-api-testing)

Engine-agnostic customer-facing process testing support for `process-engine-api`.

## TL;DR

Write customer-facing process tests against the engine-independent testing API. Add an adapter for
each process engine; the adapter connects the API to that engine, so the same tests can run without
being rewritten for a specific runtime.

This library exists to keep those tests outside an engine migration. When you replace an engine,
implement or update its adapter and run the existing tests against it. The tests then guard the
migration against regressions instead of becoming part of the migration work.

## JGiven

Use `ProcessScenarioTest` with an adapter-provided initializer. The generic stages provide the
engine-agnostic process operations; applications can extend them with domain-specific vocabulary.

```kotlin
import dev.bpmcrafters.processengineapi.testing.config.UseProcessTestInitializer
import dev.bpmcrafters.processengineapi.testing.jgiven.ActionStage
import dev.bpmcrafters.processengineapi.testing.jgiven.AssertStage
import dev.bpmcrafters.processengineapi.testing.jgiven.ProcessScenarioTest
import dev.bpmcrafters.processengineapi.testing.jgiven.given
import dev.bpmcrafters.processengineapi.testing.jgiven.then
import dev.bpmcrafters.processengineapi.testing.jgiven.whenever
import org.junit.jupiter.api.Test

@UseProcessTestInitializer(MyAdapterProcessTestInitializer.QUALIFIER)
class OrderApprovalProcessTest : ProcessScenarioTest<OrderActions, OrderAssertions>() {

  @Test
  fun `approved order completes`() {
    given {
      processIsDeployed("bpmn/order-approval.bpmn")
      processIsStartedByDefinition("order-approval", payload = mapOf("orderId" to "order-42"))
    }

    whenever {
      userTaskIsCompleted("approve-order", payload = mapOf("approved" to true))
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


## Adapter compatibility contract

`process-engine-api-testing-contract` contains reusable abstract JUnit/JGiven suites. An adapter
consumes it as a test dependency, selects its initializer, and supplies a logical fixture for
adapter-owned BPMN resources:

```kotlin
@UseProcessTestInitializer(MyAdapterProcessTestInitializer.QUALIFIER)
class MyAdapterUserTaskContract : UserTaskProcessCompatibilitySuite() {
  override fun userTaskProcessFixture() = object : UserTaskProcessFixture {
    override fun classpathResource() = "bpmn/user-task-process.bpmn"
    override fun definitionKey() = "user-task-process"
    override fun userTaskDescriptionKey() = "approve"
    override fun startElementId() = "start"
    override fun userTaskElementId() = "approve"
    override fun endElementId() = "end"
  }
}
```

The initial suite proves deployment, definition start, user-task selection and completion, ordered
path assertions, and normal completion. Further feature- and capability-specific suites are added
without changing adapter runners that already consume this artifact.

Reference C7 BPMN models are available under `contract/src/main/resources/reference/c7`. They cover
task assignment refresh, message and signal correlation, timers, externally delivered service
tasks, and alternate starts. Each has BPMN text annotations describing what another engine adapter
may need to translate.
