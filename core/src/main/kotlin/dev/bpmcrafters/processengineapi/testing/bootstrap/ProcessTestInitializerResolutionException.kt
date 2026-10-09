package dev.bpmcrafters.processengineapi.testing.bootstrap

/**
 * Thrown when no unique process-test initializer can be selected for a test class.
 */
class ProcessTestInitializerResolutionException(
  message: String,
) : RuntimeException(message)
