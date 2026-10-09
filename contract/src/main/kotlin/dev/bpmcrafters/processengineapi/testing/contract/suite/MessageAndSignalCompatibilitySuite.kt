package dev.bpmcrafters.processengineapi.testing.contract.suite

import dev.bpmcrafters.processengineapi.correlation.Correlation
import dev.bpmcrafters.processengineapi.testing.contract.fixture.MessageAndSignalFixture
import dev.bpmcrafters.processengineapi.testing.contract.fixture.definitionRestrictions
import dev.bpmcrafters.processengineapi.testing.contract.stage.ContractActionStage
import dev.bpmcrafters.processengineapi.testing.contract.stage.ContractAssertStage
import dev.bpmcrafters.processengineapi.testing.jgiven.ProcessScenarioTest
import dev.bpmcrafters.processengineapi.testing.jgiven.given
import dev.bpmcrafters.processengineapi.testing.jgiven.then
import dev.bpmcrafters.processengineapi.testing.jgiven.whenever
import org.junit.jupiter.api.Test

/**
 * Verifies intermediate message correlation and signal delivery.
 */
abstract class MessageAndSignalCompatibilitySuite :
  ProcessScenarioTest<ContractActionStage, ContractAssertStage>() {
  /**
   * @return fixture describing the message-and-signal process
   */
  protected abstract fun messageAndSignalFixture(): MessageAndSignalFixture

  /**
   * Verifies that a correlated message advances the process to its signal wait state.
   */
  @Test
  fun `adapter correlates a message to the waiting process`() {
    val fixture = messageAndSignalFixture()
    val correlationPayload = mapOf("contractMessageStatus" to "delivered")

    given {
      processIsDeployed(fixture.classpathResource())
      processIsStartedByDefinition(
        fixture.definitionKey(),
        payload = mapOf(fixture.correlationVariable() to fixture.correlationKey()),
        restrictions = fixture.definitionRestrictions(),
      )
      processWaitsInElement(fixture.messageCatchElementId())
    }

    whenever {
      messageIsCorrelated(
        fixture.messageName(),
        Correlation.withKey(fixture.correlationKey()).withVariable(fixture.correlationVariable()),
        payload = correlationPayload,
      )
    }

    then {
      processWaitsInElement(fixture.signalCatchElementId())
      processPayloadContains(correlationPayload)
    }
  }

  /**
   * Verifies that a broadcast signal completes the waiting process.
   */
  @Test
  fun `adapter delivers a signal to the waiting process`() {
    val fixture = messageAndSignalFixture()
    given {
      processIsDeployed(fixture.classpathResource())
      processIsStartedByDefinition(
        fixture.definitionKey(),
        payload = mapOf(fixture.correlationVariable() to fixture.correlationKey()),
        restrictions = fixture.definitionRestrictions(),
      )
    }

    whenever {
      messageIsCorrelated(
        fixture.messageName(),
        Correlation.withKey(fixture.correlationKey()).withVariable(fixture.correlationVariable()),
      )
      signalIsSent(fixture.signalName())
    }

    then {
      processHasPassed(fixture.signalCatchElementId(), fixture.endElementId())
      processIsFinished()
    }
  }
}
