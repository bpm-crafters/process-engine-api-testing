package dev.bpmcrafters.processengineapi.testing.contract.suite

import dev.bpmcrafters.processengineapi.testing.contract.fixture.AlternateStartFixture
import dev.bpmcrafters.processengineapi.testing.contract.fixture.definitionRestrictions
import dev.bpmcrafters.processengineapi.testing.contract.stage.ContractActionStage
import dev.bpmcrafters.processengineapi.testing.contract.stage.ContractAssertStage
import dev.bpmcrafters.processengineapi.testing.jgiven.ProcessScenarioTest
import dev.bpmcrafters.processengineapi.testing.jgiven.given
import dev.bpmcrafters.processengineapi.testing.jgiven.then
import dev.bpmcrafters.processengineapi.testing.jgiven.whenever
import org.junit.jupiter.api.Test

/**
 * Verifies message-start and element-entry start modes.
 */
abstract class AlternateStartCompatibilitySuite :
  ProcessScenarioTest<ContractActionStage, ContractAssertStage>() {
  /**
   * @return fixture describing alternate process starts
   */
  protected abstract fun alternateStartFixture(): AlternateStartFixture

  /**
   * Verifies message-start process creation.
   */
  @Test
  fun `adapter starts a process by message`() {
    val fixture = alternateStartFixture()

    given {
      processIsDeployed(fixture.classpathResource())
    }

    whenever {
      processIsStartedByMessage(
        fixture.startMessageName(),
        restrictions = fixture.definitionRestrictions(),
      )
    }

    then {
      processWaitsInUserTask(fixture.userTaskDescriptionKey())
    }
  }

  /**
   * Verifies direct entry at a BPMN element.
   */
  @Test
  fun `adapter starts a process at an element`() {
    val fixture = alternateStartFixture()
    given {
      processIsDeployed(fixture.classpathResource())
    }

    whenever {
      processIsStartedByDefinitionAtElement(
        fixture.definitionKey(),
        fixture.entryElementId(),
        restrictions = fixture.definitionRestrictions(),
      )
    }

    then {
      processWaitsInUserTask(fixture.userTaskDescriptionKey())
    }
  }
}
