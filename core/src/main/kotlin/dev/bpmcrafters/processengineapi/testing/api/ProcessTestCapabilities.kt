package dev.bpmcrafters.processengineapi.testing.api

import java.time.Duration

/**
 * Optional testing features exposed by the selected engine integration.
 */
interface ProcessTestCapabilities {
  /**
   * Returns whether this context can advance the engine clock.
   *
   * @return `true` when [timePasses] is supported
   */
  fun supportsTimeTravel(): Boolean

  /**
   * Advances the engine clock by [duration], or fails when time travel is unsupported.
   *
   * When supported, this method returns only after all engine work made due by the elapsed time is
   * observable through this context's query and assertion APIs.
   *
   * @param duration amount of engine time to advance; must not be negative
   */
  fun timePasses(duration: Duration)
}
