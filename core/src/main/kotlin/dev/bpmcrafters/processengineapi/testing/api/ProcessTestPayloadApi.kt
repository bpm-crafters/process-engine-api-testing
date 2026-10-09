package dev.bpmcrafters.processengineapi.testing.api

/**
 * Engine-agnostic payload mutation and snapshot operations for process tests.
 *
 * Payload values deliberately use the production API's broad variable representation. Adapters
 * must return snapshots: callers must not be able to mutate engine state through a returned map.
 * Mutations return only after their observable engine side effects are available to the query and
 * assertion APIs.
 */
interface ProcessTestPayloadApi {

  /**
   * Replaces or adds [payload] entries on the process instance identified by [instanceId].
   *
   * @param instanceId process instance whose variables are changed
   * @param payload variable names and values to set
   */
  fun updateProcessPayload(instanceId: String, payload: Map<String, Any?>)

  /**
   * Returns a snapshot of the current payload of [instanceId].
   *
   * @param instanceId process instance to inspect
   * @return current process variables as a detached snapshot
   */
  fun getProcessPayload(instanceId: String): Map<String, Any?>

  /**
   * Returns a snapshot of task-local payload for [taskId].
   *
   * @param taskId user or external task to inspect
   * @return current task-local variables as a detached snapshot
   */
  fun getTaskPayload(taskId: String): Map<String, Any?>
}
