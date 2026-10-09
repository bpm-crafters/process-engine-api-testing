package dev.bpmcrafters.processengineapi.testing.bootstrap

/**
 * Loads a named source of properties used to initialize a process-test context.
 *
 * Implementations must have a public no-argument constructor. They are instantiated before the
 * selected initializer runs, so they can contribute configuration required to start an application
 * context. The SPI deliberately has no dependency on a particular application framework.
 */
interface ProcessTestPropertyProvider {

  /**
   * Loads properties for the requested test.
   *
   * Properties declared directly on the test annotation are available through [request] and take
   * precedence over the returned values.
   */
  fun load(request: ProcessTestPropertyRequest): Map<String, String>
}

/**
 * Input made available while resolving process-test properties.
 *
 * @property explicitConfiguration properties declared directly on
 * [dev.bpmcrafters.processengineapi.testing.config.UseProcessTestInitializer]. This lets a provider
 * use an explicitly selected profile while loading the rest of its configuration.
 */
data class ProcessTestPropertyRequest(
  val testClass: Class<*>,
  val explicitConfiguration: Map<String, String>,
)
