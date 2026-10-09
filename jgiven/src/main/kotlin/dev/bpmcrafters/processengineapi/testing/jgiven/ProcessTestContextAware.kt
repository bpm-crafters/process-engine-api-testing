package dev.bpmcrafters.processengineapi.testing.jgiven

import dev.bpmcrafters.processengineapi.testing.api.ProcessTestContext

/**
 * Receives the process-test context created by [ProcessTestInitializerExtension].
 */
interface ProcessTestContextAware {

  /**
   * Supplies the context for the current scenario.
   *
   * @param processTestContext context created for the current scenario
   * @return no value; implementations retain the supplied scenario context.
   */
  fun processTestContext(processTestContext: ProcessTestContext)
}
