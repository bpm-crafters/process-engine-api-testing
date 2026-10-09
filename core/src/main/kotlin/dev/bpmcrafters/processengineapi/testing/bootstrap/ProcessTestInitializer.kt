package dev.bpmcrafters.processengineapi.testing.bootstrap

import dev.bpmcrafters.processengineapi.testing.api.ProcessTestContext

/**
 * Adapter-provided factory for [ProcessTestContext] instances.
 *
 * Implementations are discovered with [java.util.ServiceLoader]. They create and later clean up only
 * engine test infrastructure; customer application drivers remain the responsibility of the test.
 */
interface ProcessTestInitializer {

  /**
   * Returns the stable, adapter-specific qualifier used to select this initializer.
   *
   * @return a unique qualifier such as `c7embedded`
   */
  fun qualifier(): String

  /**
   * Creates a new, isolated process-test context for [request].
   *
   * @param request details of the test class requesting initialization
   * @return a context that the test integration closes after the scenario
   */
  fun initialize(request: ProcessTestInitializationRequest): ProcessTestContext
}
