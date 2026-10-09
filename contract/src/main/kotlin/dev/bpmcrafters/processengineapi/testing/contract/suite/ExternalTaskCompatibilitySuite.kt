package dev.bpmcrafters.processengineapi.testing.contract.suite

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.testing.contract.fixture.ExternalTaskProcessFixture
import dev.bpmcrafters.processengineapi.testing.contract.fixture.definitionRestrictions
import dev.bpmcrafters.processengineapi.testing.contract.stage.ContractActionStage
import dev.bpmcrafters.processengineapi.testing.contract.stage.ContractAssertStage
import dev.bpmcrafters.processengineapi.testing.jgiven.ProcessScenarioTest
import dev.bpmcrafters.processengineapi.testing.jgiven.given
import dev.bpmcrafters.processengineapi.testing.jgiven.then
import dev.bpmcrafters.processengineapi.testing.jgiven.whenever
import org.junit.jupiter.api.Test

/**
 * Verifies the mocked-worker flow through an externally delivered service task.
 */
abstract class ExternalTaskCompatibilitySuite :
  ProcessScenarioTest<ContractActionStage, ContractAssertStage>() {
  /**
   * @return fixture describing the external-task process
   */
  protected abstract fun externalTaskProcessFixture(): ExternalTaskProcessFixture

  /**
   * Verifies external-task selection, completion, and process completion.
   */
  @Test
  fun `adapter delivers and completes an external task`() {
    val fixture = externalTaskProcessFixture()
    given {
      processIsDeployed(fixture.classpathResource())
      processIsStartedByDefinition(fixture.definitionKey(), restrictions = fixture.definitionRestrictions())
    }

    whenever {
      externalTaskIsCompleted(fixture.externalTaskDescriptionKey())
    }

    then {
      processHasPassed(fixture.endElementId())
      processIsFinished()
    }
  }
}
