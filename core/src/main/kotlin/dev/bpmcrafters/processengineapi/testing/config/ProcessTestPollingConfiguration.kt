package dev.bpmcrafters.processengineapi.testing.config

import java.time.Duration

/**
 * Poll interval and maximum wait time used by adapter assertion implementations.
 *
 * @param pollInterval delay between assertion attempts
 * @param timeout maximum duration an eventually consistent assertion may wait
 */
data class ProcessTestPollingConfiguration(
  /**
   * Time to wait between assertion attempts.
   */
  val pollInterval: Duration,
  /**
   * Maximum time an eventually consistent assertion may wait.
   */
  val timeout: Duration,
)
