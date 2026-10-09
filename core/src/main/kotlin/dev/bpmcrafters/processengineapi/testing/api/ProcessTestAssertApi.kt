package dev.bpmcrafters.processengineapi.testing.api

import dev.bpmcrafters.processengineapi.RestrictionAware
import dev.bpmcrafters.processengineapi.task.TaskInformation
import java.util.function.Predicate

/**
 * Engine-agnostic, eventually consistent assertions for a process test.
 *
 * Implementations apply supported [restrictions] before an optional task [predicate]. Task-selection
 * methods must fail when the resulting task set is empty or ambiguous. Implementations own polling,
 * timeouts and diagnostic failure messages; callers must not implement their own waiting loop.
 */
interface ProcessTestAssertApi : RestrictionAware {

  /**
   * Waits until [instanceId] has exactly one matching user task and returns its snapshot.
   *
   * @param instanceId process instance to inspect
   * @param taskDescriptionKey optional engine-agnostic user-task key
   * @param restrictions supported restrictions applied before [predicate]
   * @param predicate optional additional filter evaluated after [restrictions]
   * @return the one selected user task
   */
  fun processWaitsInUserTask(
    instanceId: String,
    taskDescriptionKey: String?,
    restrictions: Map<String, String>,
    predicate: Predicate<TaskInformation>?,
  ): TaskInformation

  /**
   * Waits until [instanceId] has an active token in [elementId].
   *
   * @param instanceId process instance to inspect
   * @param elementId BPMN element identifier that must be active
   * @param restrictions supported restrictions that scope the assertion
   */
  fun processWaitsInElement(
    instanceId: String,
    elementId: String,
    restrictions: Map<String, String>,
  )

  /**
   * Waits until exactly one matching user task exists and returns its snapshot.
   *
   * @param taskDescriptionKey optional engine-agnostic user-task key
   * @param restrictions supported restrictions applied before [predicate]
   * @param predicate optional additional filter evaluated after [restrictions]
   * @return the one selected user task
   */
  fun userTaskIsSelected(
    taskDescriptionKey: String?,
    restrictions: Map<String, String>,
    predicate: Predicate<TaskInformation>?,
  ): TaskInformation

  /**
   * Waits until exactly one matching external task exists and returns its snapshot.
   *
   * @param taskDescriptionKey optional engine-agnostic external-task key
   * @param restrictions supported restrictions applied before [predicate]
   * @param predicate optional additional filter evaluated after [restrictions]
   * @return the one selected external task
   */
  fun externalTaskIsSelected(
    taskDescriptionKey: String?,
    restrictions: Map<String, String>,
    predicate: Predicate<TaskInformation>?,
  ): TaskInformation

  /**
   * Verifies that [instanceId] passed every supplied BPMN [elementIds].
   *
   * @param instanceId process instance to inspect
   * @param restrictions supported restrictions that scope the assertion
   * @param elementIds BPMN element identifiers that must have been passed
   */
  fun processHasPassed(
    instanceId: String,
    restrictions: Map<String, String>,
    vararg elementIds: String,
  )

  /**
   * Verifies that [instanceId] passed [elementIds] in their supplied order.
   *
   * @param instanceId process instance to inspect
   * @param restrictions supported restrictions that scope the assertion
   * @param elementIds BPMN element identifiers that must have been passed in order
   */
  fun processHasPassedInOrder(
    instanceId: String,
    restrictions: Map<String, String>,
    vararg elementIds: String,
  )

  /**
   * Verifies that [instanceId] has not passed any supplied BPMN [elementIds].
   *
   * @param instanceId process instance to inspect
   * @param restrictions supported restrictions that scope the assertion
   * @param elementIds BPMN element identifiers that must not have been passed
   */
  fun processHasNotPassed(
    instanceId: String,
    restrictions: Map<String, String>,
    vararg elementIds: String,
  )

  /**
   * Waits until [instanceId] has completed.
   *
   * @param instanceId process instance to inspect
   * @param restrictions supported restrictions that scope the assertion
   */
  fun processIsFinished(
    instanceId: String,
    restrictions: Map<String, String>,
  )

  /**
   * Waits until [instanceId] has at least one incident.
   *
   * @param instanceId process instance to inspect
   * @param restrictions supported restrictions that scope the assertion
   */
  fun processHasIncidents(
    instanceId: String,
    restrictions: Map<String, String>,
  )
}
