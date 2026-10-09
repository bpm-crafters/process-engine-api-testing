package dev.bpmcrafters.processengineapi.testing.api

import dev.bpmcrafters.processengineapi.RestrictionAware
import dev.bpmcrafters.processengineapi.process.ProcessInformation
import dev.bpmcrafters.processengineapi.task.TaskInformation
import java.util.function.Predicate

/**
 * Snapshot-only, engine-agnostic process-test queries.
 *
 * These methods never poll or wait. They apply supported [restrictions] first and an optional
 * [predicate] second, returning every match so advanced stages can apply their own interpretation.
 */
interface ProcessTestQueryApi : RestrictionAware {

  /**
   * Returns the user-task snapshots matching the supplied criteria at the time of the call.
   *
   * @param taskDescriptionKey optional engine-agnostic user-task key
   * @param restrictions supported restrictions applied before [predicate]
   * @param predicate optional additional filter evaluated after [restrictions]
   * @return every matching user-task snapshot; never waits for a future task
   */
  fun findUserTasks(
    taskDescriptionKey: String?,
    restrictions: Map<String, String>,
    predicate: Predicate<TaskInformation>?,
  ): List<TaskInformation>

  /**
   * Returns the external-task snapshots matching the supplied criteria at the time of the call.
   *
   * @param taskDescriptionKey optional engine-agnostic external-task key
   * @param restrictions supported restrictions applied before [predicate]
   * @param predicate optional additional filter evaluated after [restrictions]
   * @return every matching external-task snapshot; never waits for a future task
   */
  fun findExternalTasks(
    taskDescriptionKey: String?,
    restrictions: Map<String, String>,
    predicate: Predicate<TaskInformation>?,
  ): List<TaskInformation>

  /**
   * Returns the current process snapshot for [instanceId], when the engine can still query it.
   *
   * @param instanceId process instance to inspect
   * @return the current snapshot, or `null` when no snapshot is available
   */
  fun getProcessInformation(instanceId: String): ProcessInformation?
}
