package dev.bpmcrafters.processengineapi.testing.api

import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SignalApi
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.UserTaskModificationApi

/**
 * Existing `process-engine-api` runtime operations made available to a process test.
 *
 * This interface deliberately reuses the production API rather than exposing engine runtime types.
 *
 * Adapters must synchronize every process-affecting asynchronous operation before the operation's
 * returned completion stage completes. Completion therefore means that the operation's observable
 * engine side effects are available to the query and assertion APIs; it must not mean merely that
 * the engine accepted the command. The concrete synchronization strategy is adapter-owned, for
 * example executing controlled deferred work in an embedded engine or awaiting its normal executor
 * in a live engine. Customer scenarios use eventual assertion methods to synchronize on a specific
 * expected outcome and never use a separate continuation operation or polling library.
 */
interface ProcessTestRuntimeApis {

  /**
   * Returns the API for starting process instances.
   */
  fun startProcessApi(): StartProcessApi

  /**
   * Returns the task-subscription API for advanced test scenarios.
   */
  fun taskSubscriptionApi(): TaskSubscriptionApi

  /**
   * Returns the user-task completion API.
   */
  fun userTaskCompletionApi(): UserTaskCompletionApi

  /**
   * Returns the user-task modification API.
   *
   * @return API used to update assignment, dates, or task-local payload
   */
  fun userTaskModificationApi(): UserTaskModificationApi

  /**
   * Returns the external/service-task completion API.
   */
  fun serviceTaskCompletionApi(): ServiceTaskCompletionApi

  /**
   * Returns the message-correlation API.
   */
  fun correlationApi(): CorrelationApi

  /**
   * Returns the signal-delivery API.
   */
  fun signalApi(): SignalApi

  /**
   * Returns the process-resource deployment API.
   */
  fun deploymentApi(): DeploymentApi
}
