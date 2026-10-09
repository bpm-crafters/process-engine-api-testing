package dev.bpmcrafters.processengineapi.testing.config

import dev.bpmcrafters.processengineapi.testing.bootstrap.ProcessTestPropertyProvider
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Selects the adapter-specific process-test initializer for a test class hierarchy.
 *
 * When absent, initialization is allowed only if exactly one initializer is discovered.
 *
 * @property qualifier stable initializer qualifier, for example `c7embedded`
 * @property configuration explicit `key=value` settings passed to the initializer; these override
 * properties loaded by [propertyProviders]
 * @property propertyProviders framework- or adapter-specific sources that contribute properties
 * before the initializer runs
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
annotation class UseProcessTestInitializer(
  val qualifier: String,
  val configuration: Array<String> = [],
  val propertyProviders: Array<KClass<out ProcessTestPropertyProvider>> = [],
)
