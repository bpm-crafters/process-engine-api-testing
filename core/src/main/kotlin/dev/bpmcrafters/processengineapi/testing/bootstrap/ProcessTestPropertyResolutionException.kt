package dev.bpmcrafters.processengineapi.testing.bootstrap

/** Thrown when a configured process-test property provider cannot be created or used. */
class ProcessTestPropertyResolutionException(
  message: String,
  cause: Throwable? = null,
) : RuntimeException(message, cause)
