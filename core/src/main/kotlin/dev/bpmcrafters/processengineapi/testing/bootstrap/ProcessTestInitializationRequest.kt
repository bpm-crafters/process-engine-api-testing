package dev.bpmcrafters.processengineapi.testing.bootstrap

import dev.bpmcrafters.processengineapi.testing.config.UseProcessTestInitializer

/**
 * Input supplied to an initializer for a single test-scenario context.
 *
 * @param testClass concrete JUnit test class requesting initialization
 * @param configuration adapter-specific settings selected by the test class
 */
data class ProcessTestInitializationRequest(
  /**
   * The concrete JUnit test class requesting the context.
   */
  val testClass: Class<*>,
  /**
   * Adapter-specific configuration parsed from [UseProcessTestInitializer].
   */
  val configuration: Map<String, String> = emptyMap(),
)
