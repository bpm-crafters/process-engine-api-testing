package dev.bpmcrafters.processengineapi.testing.contract.suite

import dev.bpmcrafters.processengineapi.testing.contract.fixture.UserTaskAssignmentFixture
import dev.bpmcrafters.processengineapi.testing.contract.fixture.activityRestrictions
import dev.bpmcrafters.processengineapi.testing.contract.fixture.definitionRestrictions
import dev.bpmcrafters.processengineapi.testing.contract.stage.ContractActionStage
import dev.bpmcrafters.processengineapi.testing.contract.stage.ContractAssertStage
import dev.bpmcrafters.processengineapi.testing.jgiven.ProcessScenarioTest
import dev.bpmcrafters.processengineapi.testing.jgiven.given
import dev.bpmcrafters.processengineapi.testing.jgiven.then
import dev.bpmcrafters.processengineapi.testing.jgiven.whenever
import org.junit.jupiter.api.Test

/**
 * Verifies that a user-task mutation is visible in a subsequent task snapshot.
 */
abstract class UserTaskAssignmentCompatibilitySuite : ProcessScenarioTest<ContractActionStage, ContractAssertStage>() {
  /**
   * @return fixture describing the assignment process
   */
  protected abstract fun userTaskAssignmentFixture(): UserTaskAssignmentFixture

  /** Verifies the adapter exposes the initial task assignment. */
  @Test
  fun `adapter exposes the initial user-task assignment`() {
    val fixture = userTaskAssignmentFixture()

    given {
      processIsDeployed(fixture.classpathResource())
      processIsStartedByDefinition(
        fixture.definitionKey(),
        restrictions = fixture.definitionRestrictions(),
      )
    }

    then {
      processWaitsInUserTask(
        fixture.userTaskDescriptionKey(),
        restrictions = fixture.activityRestrictions(fixture.userTaskDescriptionKey()),
      )
      selectedTaskHasMetadata(fixture.assigneeMetadataKey(), fixture.initialAssignee())
    }
  }

  /** Verifies assignment mutation and snapshot refresh. */
  @Test
  fun `adapter exposes a changed user-task assignment`() {
    val fixture = userTaskAssignmentFixture()
    val startPayload = mapOf("contractRequestId" to "request-created")
    val updatedProcessPayload = mapOf("contractRequestId" to "request-updated")
    val updatedTaskPayload = mapOf("contractDecision" to "approved")

    given {
      processIsDeployed(fixture.classpathResource())
      processIsStartedByDefinition(
        fixture.definitionKey(),
        payload = startPayload,
        restrictions = fixture.definitionRestrictions(),
      )
      processWaitsInUserTask(
        fixture.userTaskDescriptionKey(),
        restrictions = fixture.activityRestrictions(fixture.userTaskDescriptionKey()),
      )
    }

    whenever {
      selectedUserTaskIsAssignedTo(fixture.assignee())
      currentProcessPayloadIsUpdated(updatedProcessPayload)
      selectedUserTaskPayloadIsUpdated(updatedTaskPayload)
    }

    then {
      userTaskIsSelected(
        fixture.userTaskDescriptionKey(),
        restrictions = fixture.activityRestrictions(fixture.userTaskDescriptionKey()),
      )
      selectedTaskHasMetadata(fixture.assigneeMetadataKey(), fixture.assignee())
      processPayloadContains(updatedProcessPayload)
      selectedTaskPayloadContains(updatedTaskPayload)
    }
  }
}
