package dev.bpmcrafters.processengineapi.testing.contract.suite

import dev.bpmcrafters.processengineapi.testing.contract.fixture.UserTaskProcessFixture
import dev.bpmcrafters.processengineapi.testing.contract.fixture.definitionRestrictions
import dev.bpmcrafters.processengineapi.testing.contract.stage.ContractActionStage
import dev.bpmcrafters.processengineapi.testing.contract.stage.ContractAssertStage
import dev.bpmcrafters.processengineapi.testing.jgiven.ProcessScenarioTest
import dev.bpmcrafters.processengineapi.testing.jgiven.Step
import dev.bpmcrafters.processengineapi.testing.jgiven.given
import dev.bpmcrafters.processengineapi.testing.jgiven.step
import dev.bpmcrafters.processengineapi.testing.jgiven.then
import dev.bpmcrafters.processengineapi.testing.jgiven.whenever
import org.junit.jupiter.api.Test

/**
 * Reusable compatibility suite for a process with one user task and a normal completion path.
 *
 * A concrete adapter test extends this class, selects its initializer with
 * `@UseProcessTestInitializer`, and returns a fixture that describes adapter-owned BPMN. The suite
 * deliberately uses only the public testing DSL and does not inspect adapter internals.
 */
abstract class UserTaskProcessCompatibilitySuite :
  ProcessScenarioTest<ContractActionStage, ContractAssertStage>() {

  /**
   * Returns the logical process model used by this suite.
   *
   * @return fixture describing the adapter-owned user-task process
   */
  protected abstract fun userTaskProcessFixture(): UserTaskProcessFixture

  /**
   * Verifies deployment, process start, task selection, and the path to the active user task.
   */
  @Test
  fun `adapter deploys starts and observes a user-task process`() {
    val fixture = userTaskProcessFixture()
    val deploy: Step<ContractActionStage> = step {
      processIsDeployed(fixture.classpathResource())
    }

    given(deploy)

    whenever {
      processIsStartedByDefinition(fixture.definitionKey(), restrictions = fixture.definitionRestrictions())
    }

    then {
      processWaitsInUserTask(fixture.userTaskDescriptionKey())
      processHasPassedInOrder(
        fixture.startElementId(),
        fixture.userTaskElementId(),
      )
    }
  }

  /**
   * Verifies completion of a selected user task and the normal completion path.
   */
  @Test
  fun `adapter completes a user task and finishes the process`() {
    val fixture = userTaskProcessFixture()

    given {
      processIsDeployed(fixture.classpathResource())
      processIsStartedByDefinition(fixture.definitionKey(), restrictions = fixture.definitionRestrictions())
    }

    whenever {
      userTaskIsCompleted(fixture.userTaskDescriptionKey())
    }

    then {
      processHasPassedInOrder(
        fixture.startElementId(),
        fixture.userTaskElementId(),
        fixture.endElementId(),
      )
      processIsFinished()
    }
  }
}
