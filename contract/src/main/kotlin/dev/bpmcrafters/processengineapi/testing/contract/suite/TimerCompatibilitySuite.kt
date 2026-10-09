package dev.bpmcrafters.processengineapi.testing.contract.suite

import dev.bpmcrafters.processengineapi.testing.contract.fixture.TimerProcessFixture
import dev.bpmcrafters.processengineapi.testing.contract.fixture.definitionRestrictions
import dev.bpmcrafters.processengineapi.testing.contract.stage.ContractActionStage
import dev.bpmcrafters.processengineapi.testing.contract.stage.ContractAssertStage
import dev.bpmcrafters.processengineapi.testing.jgiven.ProcessScenarioTest
import dev.bpmcrafters.processengineapi.testing.jgiven.given
import dev.bpmcrafters.processengineapi.testing.jgiven.then
import dev.bpmcrafters.processengineapi.testing.jgiven.whenever
import org.junit.jupiter.api.Test
import java.time.Duration

/**
 * Capability suite for adapters that support advancing their process-engine clock.
 */
abstract class TimerCompatibilitySuite :
  ProcessScenarioTest<ContractActionStage, ContractAssertStage>() {
  /**
   * @return fixture describing the timer process
   */
  protected abstract fun timerProcessFixture(): TimerProcessFixture

  /**
   * Verifies that advancing engine time releases the timer wait.
   */
  @Test
  fun `adapter triggers a timer when engine time advances`() {
    val fixture = timerProcessFixture()
    given {
      processIsDeployed(fixture.classpathResource())
      processIsStartedByDefinition(fixture.definitionKey(), restrictions = fixture.definitionRestrictions())
      processWaitsInElement(fixture.timerElementId())
    }

    whenever {
      timePasses(Duration.ofMinutes(5))
    }

    then {
      processWaitsInUserTask(fixture.userTaskDescriptionKey())
    }
  }
}
