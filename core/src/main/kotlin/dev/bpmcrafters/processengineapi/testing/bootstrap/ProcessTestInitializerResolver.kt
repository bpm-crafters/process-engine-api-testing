package dev.bpmcrafters.processengineapi.testing.bootstrap

import dev.bpmcrafters.processengineapi.testing.config.UseProcessTestInitializer
import java.util.*
import kotlin.reflect.KClass

/**
 * Resolves the one initializer selected for a JUnit test class.
 */
object ProcessTestInitializerResolver {

  /**
   * Returns the resolved adapter-specific configuration selected by the test class hierarchy.
   */
  fun configuration(testClass: Class<*>): Map<String, String> =
    findInitializerAnnotation(testClass)
      ?.let { annotation ->
        val explicitConfiguration = annotation.configuration.fold(linkedMapOf<String, String>()) { configuration, setting ->
          val separator = setting.indexOf('=')
          require(separator > 0) { "Invalid process test initializer configuration '$setting'. Expected key=value." }
          val key = setting.substring(0, separator)
          require(configuration.put(key, setting.substring(separator + 1)) == null) {
            "Duplicate process test initializer configuration '$key'."
          }
          configuration
        }
        val providerRequest = ProcessTestPropertyRequest(testClass, explicitConfiguration)
        val providedConfiguration = annotation.propertyProviders.fold(linkedMapOf<String, String>()) { properties, providerClass ->
          properties.apply { putAll(createProvider(providerClass).load(providerRequest)) }
        }
        providedConfiguration.apply { putAll(explicitConfiguration) }
      }
      ?: emptyMap()

  private fun createProvider(
    providerClass: KClass<out ProcessTestPropertyProvider>,
  ): ProcessTestPropertyProvider =
    try {
      providerClass.java.getDeclaredConstructor().newInstance()
    } catch (exception: ReflectiveOperationException) {
      throw ProcessTestPropertyResolutionException(
        "Could not create process-test property provider ${providerClass.qualifiedName}. " +
          "Property providers must have a public no-argument constructor.",
        exception,
      )
    }

  /**
   * Discovers initializers with [ServiceLoader] using [testClass]'s class loader and selects one.
   *
   * An inherited [UseProcessTestInitializer] qualifier wins; otherwise discovery must yield exactly
   * one initializer.
   *
   * @param testClass concrete JUnit test class requiring a process-test context
   * @return the uniquely selected initializer
   * @throws ProcessTestInitializerResolutionException when selection is absent or ambiguous
   */
  fun resolve(testClass: Class<*>): ProcessTestInitializer =
    resolve(
      testClass = testClass,
      initializers = ServiceLoader.load(ProcessTestInitializer::class.java, testClass.classLoader).toList(),
    )

  internal fun resolve(
    testClass: Class<*>,
    initializers: List<ProcessTestInitializer>,
  ): ProcessTestInitializer {
    val selectedQualifier = findQualifier(testClass)
    if (selectedQualifier != null) {
      val matchingInitializers = initializers.filter { it.qualifier() == selectedQualifier }
      return when (matchingInitializers.size) {
        1 -> matchingInitializers.single()
        0 -> throw ProcessTestInitializerResolutionException(
          "No process test initializer with qualifier '$selectedQualifier' was found for ${testClass.name}. " +
            "Discovered initializers: ${describe(initializers)}."
        )

        else -> throw ProcessTestInitializerResolutionException(
          "Multiple process test initializers with qualifier '$selectedQualifier' were found for ${testClass.name}. " +
            "Discovered initializers: ${describe(initializers)}."
        )
      }
    }

    return when (initializers.size) {
      1 -> initializers.single()
      0 -> throw ProcessTestInitializerResolutionException(
        "No process test initializer was discovered for ${testClass.name}. " +
          "Add a dependency that provides an initializer or annotate the test with @UseProcessTestInitializer."
      )

      else -> throw ProcessTestInitializerResolutionException(
        "Multiple process test initializers were discovered for ${testClass.name} but none was selected explicitly. " +
          "Add @UseProcessTestInitializer to the test class hierarchy. Discovered initializers: ${describe(initializers)}."
      )
    }
  }

  private fun findQualifier(testClass: Class<*>): String? = findInitializerAnnotation(testClass)?.qualifier

  private fun findInitializerAnnotation(testClass: Class<*>): UseProcessTestInitializer? {
    var currentClass: Class<*>? = testClass
    while (currentClass != null) {
      currentClass.getAnnotation(UseProcessTestInitializer::class.java)?.let { return it }
      currentClass = currentClass.superclass
    }
    return null
  }

  private fun describe(initializers: List<ProcessTestInitializer>): String =
    if (initializers.isEmpty()) {
      "<none>"
    } else {
      initializers.joinToString(", ") { "'${it.qualifier()}' -> ${it::class.java.name}" }
    }
}
