package dev.bpmcrafters.processengineapi.testing.api

/**
 * The single engine-agnostic root object for a process-test scenario.
 *
 * A context is created by a process-test initializer, used for exactly one scenario and closed by
 * the test integration after that scenario. Adapter implementations may release engine resources in
 * [close].
 */
interface ProcessTestContext : AutoCloseable {

  /**
   * Returns the stable qualifier of the initializer that created this context.
   */
  fun qualifier(): String

  /**
   * Returns process-engine APIs used to drive the scenario.
   */
  fun runtime(): ProcessTestRuntimeApis

  /**
   * Returns snapshot-only process and task queries.
   */
  fun query(): ProcessTestQueryApi

  /**
   * Returns polling assertions and strict task-selection operations.
   */
  fun assertions(): ProcessTestAssertApi

  /**
   * Returns process- and task-payload mutation and snapshot operations.
   */
  fun payloads(): ProcessTestPayloadApi

  /**
   * Returns optional engine testing capabilities.
   */
  fun capabilities(): ProcessTestCapabilities

  /**
   * Returns diagnostic configuration useful when an assertion fails.
   */
  fun diagnostics(): ProcessTestDiagnostics
}
