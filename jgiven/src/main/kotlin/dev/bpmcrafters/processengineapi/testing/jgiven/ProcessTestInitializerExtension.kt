package dev.bpmcrafters.processengineapi.testing.jgiven

import dev.bpmcrafters.processengineapi.testing.api.ProcessTestContext
import dev.bpmcrafters.processengineapi.testing.bootstrap.ProcessTestInitializationRequest
import dev.bpmcrafters.processengineapi.testing.bootstrap.ProcessTestInitializerResolver
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace

/**
 * JUnit extension that creates a context before, and closes it after, each process-test scenario.
 */
class ProcessTestInitializerExtension : BeforeEachCallback, AfterEachCallback {

  companion object {
    private val NAMESPACE: Namespace = Namespace.create(ProcessTestInitializerExtension::class.java)
  }


  override fun beforeEach(context: ExtensionContext) {
    val testInstance = requireNotNull(context.requiredTestInstance) {
      "Expected a JUnit test instance for process test initialization."
    }
    val initializer = ProcessTestInitializerResolver.resolve(context.requiredTestClass)
    val processTestContext = initializer.initialize(
      ProcessTestInitializationRequest(
        testClass = context.requiredTestClass,
        configuration = ProcessTestInitializerResolver.configuration(context.requiredTestClass),
      )
    )

    require(testInstance is ProcessTestContextAware) {
      "Test instance ${testInstance::class.java.name} must implement ${ProcessTestContextAware::class.java.name}."
    }
    testInstance.processTestContext(processTestContext)
    context.getStore(NAMESPACE).put(context.uniqueId, processTestContext)
  }

  override fun afterEach(context: ExtensionContext) {
    context.getStore(NAMESPACE)
      .remove(context.uniqueId, ProcessTestContext::class.java)
      ?.close()
  }
}
