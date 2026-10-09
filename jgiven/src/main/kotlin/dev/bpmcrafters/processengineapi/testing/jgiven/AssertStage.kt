package dev.bpmcrafters.processengineapi.testing.jgiven

import com.tngtech.jgiven.annotation.Description
import com.tngtech.jgiven.annotation.Format
import com.tngtech.jgiven.annotation.Hidden
import com.tngtech.jgiven.annotation.Quoted
import dev.bpmcrafters.processengineapi.CommonRestrictions.builder
import dev.bpmcrafters.processengineapi.testing.jgiven.format.QuotedElementIdsFormatter

/**
 * JGiven base stage providing engine-agnostic process outcome assertions.
 */
abstract class AssertStage<SUBTYPE : AssertStage<SUBTYPE>> : ProcessStage<SUBTYPE>() {

  /**
   * Verifies that the current process passed every supplied element.
   * @param elementIds BPMN element identifiers that must have been passed.
   * @return this concrete assertion stage for Java/JGiven chaining.
   */
  @Description("process has passed \$elementIds")
  open fun processHasPassed(
    @Format(QuotedElementIdsFormatter::class) vararg elementIds: String,
  ): SUBTYPE = step {
    processHasPassed(builder().build(), *elementIds)
  }

  /**
   * Verifies that the current process passed every supplied element under restrictions.
   * @param restrictions restrictions that scope the assertion.
   * @param elementIds BPMN element identifiers that must have been passed.
   * @return this concrete assertion stage for Java/JGiven chaining.
   */
  @Description("process has passed \$elementIds")
  open fun processHasPassed(
    @Hidden restrictions: Map<String, String> = builder().build(),
    @Format(QuotedElementIdsFormatter::class) vararg elementIds: String,
  ): SUBTYPE = step {
    assertApi().processHasPassed(requireInstanceId(), restrictions, *elementIds)
    refreshProcessInformation()
  }

  /**
   * Verifies that the current process passed supplied elements in order.
   * @param elementIds BPMN element identifiers in the required order.
   * @return this concrete assertion stage for Java/JGiven chaining.
   */
  @Description("process has passed \$elementIds in order")
  open fun processHasPassedInOrder(
    @Format(QuotedElementIdsFormatter::class) vararg elementIds: String,
  ): SUBTYPE = step {
    processHasPassedInOrder(builder().build(), *elementIds)
  }

  /**
   * Verifies ordered element passage under restrictions.
   * @param restrictions restrictions that scope the assertion.
   * @param elementIds BPMN element identifiers in the required order.
   * @return this concrete assertion stage for Java/JGiven chaining.
   */
  @Description("process has passed \$elementIds in order")
  open fun processHasPassedInOrder(
    @Hidden restrictions: Map<String, String> = builder().build(),
    @Format(QuotedElementIdsFormatter::class) vararg elementIds: String,
  ): SUBTYPE = step {
    assertApi().processHasPassedInOrder(requireInstanceId(), restrictions, *elementIds)
    refreshProcessInformation()
  }

  /**
   * Verifies that the current process passed none of the supplied elements.
   * @param elementIds BPMN element identifiers that must not have been passed.
   * @return this concrete assertion stage for Java/JGiven chaining.
   */
  @Description("process has not passed \$elementIds")
  open fun processHasNotPassed(
    @Format(QuotedElementIdsFormatter::class) vararg elementIds: String,
  ): SUBTYPE = step {
    processHasNotPassed(builder().build(), *elementIds)
  }

  /**
   * Verifies that the current process passed none of the supplied elements under restrictions.
   * @param restrictions restrictions that scope the assertion.
   * @param elementIds BPMN element identifiers that must not have been passed.
   * @return this concrete assertion stage for Java/JGiven chaining.
   */
  @Description("process has not passed \$elementIds")
  open fun processHasNotPassed(
    @Hidden restrictions: Map<String, String> = builder().build(),
    @Format(QuotedElementIdsFormatter::class) vararg elementIds: String,
  ): SUBTYPE = step {
    assertApi().processHasNotPassed(requireInstanceId(), restrictions, *elementIds)
    refreshProcessInformation()
  }

  /**
   * Waits until the current process has finished.
   * @param restrictions restrictions that scope the assertion.
   * @return this concrete assertion stage for Java/JGiven chaining.
   */
  @JvmOverloads
  @Description("process is finished")
  open fun processIsFinished(
    @Hidden restrictions: Map<String, String> = builder().build(),
  ): SUBTYPE = step {
    assertApi().processIsFinished(requireInstanceId(), restrictions)
    refreshProcessInformation()
  }

  /**
   * Waits until the current process has an incident.
   * @param restrictions restrictions that scope the assertion.
   * @return this concrete assertion stage for Java/JGiven chaining.
   */
  @Description("process has incidents")
  open fun processHasIncidents(@Hidden restrictions: Map<String, String>): SUBTYPE = step {
    assertApi().processHasIncidents(requireInstanceId(), restrictions)
    refreshProcessInformation()
  }

  /**
   * Verifies that a previously captured operation threw the expected type.
   * @param exceptionType expected throwable type.
   * @return this concrete assertion stage for Java/JGiven chaining.
   */
  @Description("an exception of type \$exceptionType was thrown")
  open fun anExceptionWasThrown(exceptionType: Class<out Throwable>): SUBTYPE = step {
    val throwable = requireNotNull(throwableCaught) {
      "Expected an exception to be captured before asserting it."
    }
    require(exceptionType.isInstance(throwable)) {
      "Expected exception of type ${exceptionType.name}, but got ${throwable::class.java.name}."
    }
  }

  /**
   * Verifies metadata on the selected task.
   * @param key metadata key to inspect.
   * @param value expected metadata value.
   * @return this concrete assertion stage for Java/JGiven chaining.
   */
  @Description("selected task has metadata \$key with value \$value")
  open fun selectedTaskHasMetadata(@Quoted key: String, @Quoted value: String): SUBTYPE = step {
    val actualValue = requireCurrentTask().meta[key]
    require(actualValue == value) {
      "Expected selected task metadata '$key' to be '$value', but was '$actualValue'."
    }
  }

  /**
   * Verifies that the current process payload contains all expected entries.
   * @param expectedPayload expected variable names and values.
   * @return this concrete assertion stage for Java/JGiven chaining.
   */
  @Description("process payload contains the expected values")
  open fun processPayloadContains(@Hidden expectedPayload: Map<String, Any?>): SUBTYPE = step {
    val payload = payloadApi().getProcessPayload(requireInstanceId())
    require(expectedPayload.all { (key, value) -> payload[key] == value }) {
      "Expected process payload to contain $expectedPayload, but was $payload."
    }
  }

  /**
   * Verifies that the selected task payload contains all expected entries.
   * @param expectedPayload expected variable names and values.
   * @return this concrete assertion stage for Java/JGiven chaining.
   */
  @Description("selected task payload contains the expected values")
  open fun selectedTaskPayloadContains(@Hidden expectedPayload: Map<String, Any?>): SUBTYPE = step {
    val payload = payloadApi().getTaskPayload(requireCurrentTask().taskId)
    require(expectedPayload.all { (key, value) -> payload[key] == value }) {
      "Expected selected task payload to contain $expectedPayload, but was $payload."
    }
  }
}
