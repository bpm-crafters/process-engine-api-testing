package dev.bpmcrafters.processengineapi.testing.api

import dev.bpmcrafters.processengineapi.testing.config.ProcessTestPollingConfiguration

/**
 * Diagnostic data describing how this process-test context evaluates assertions.
 *
 * @param pollingConfiguration polling behavior used by adapter assertion implementations
 */
data class ProcessTestDiagnostics(
  /**
   * The polling configuration used by assertion operations.
   */
  val pollingConfiguration: ProcessTestPollingConfiguration,
)
