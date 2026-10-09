package dev.bpmcrafters.processengineapi.testing.bootstrap

import dev.bpmcrafters.processengineapi.testing.api.ProcessTestContext
import dev.bpmcrafters.processengineapi.testing.config.UseProcessTestInitializer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ProcessTestInitializerResolverTest {

  @Test
  fun `uses explicit qualifier from concrete test class`() {
    val resolved = ProcessTestInitializerResolver.resolve(
      AnnotatedChildTest::class.java,
      listOf(FakeInitializer("c7remote"), FakeInitializer("c8")),
    )

    assertThat(resolved.qualifier()).isEqualTo("c8")
  }

  @Test
  fun `inherits qualifier from abstract superclass`() {
    val resolved = ProcessTestInitializerResolver.resolve(
      InheritingChildTest::class.java,
      listOf(FakeInitializer("c7remote"), FakeInitializer("c8")),
    )

    assertThat(resolved.qualifier()).isEqualTo("c7remote")
  }

  @Test
  fun `reads initializer configuration from the annotation`() {
    assertThat(ProcessTestInitializerResolver.configuration(ConfiguredTest::class.java))
      .containsExactlyInAnyOrderEntriesOf(mapOf("c8.deployment-propagation-delay" to "PT2S"))
  }

  @Test
  fun `merges provider properties while allowing explicit configuration to override them`() {
    ProfileAwarePropertiesProvider.lastRequest = null

    assertThat(ProcessTestInitializerResolver.configuration(PropertyProvidedTest::class.java))
      .containsExactlyInAnyOrderEntriesOf(
        mapOf(
          "spring.profiles.active" to "process-test",
          "adapter.endpoint" to "http://test-override",
          "adapter.loaded-from-profile" to "process-test",
        ),
      )
    assertThat(ProfileAwarePropertiesProvider.lastRequest?.explicitConfiguration)
      .containsEntry("spring.profiles.active", "process-test")
  }

  @Test
  fun `fails if explicit qualifier is missing`() {
    assertThatThrownBy {
      ProcessTestInitializerResolver.resolve(
        AnnotatedChildTest::class.java,
        listOf(FakeInitializer("c7remote")),
      )
    }
      .isInstanceOf(ProcessTestInitializerResolutionException::class.java)
      .hasMessageContaining("qualifier 'c8'")
  }

  @Test
  fun `auto-selects single initializer when annotation is missing`() {
    val resolved = ProcessTestInitializerResolver.resolve(
      PlainTest::class.java,
      listOf(FakeInitializer("c7remote")),
    )

    assertThat(resolved.qualifier()).isEqualTo("c7remote")
  }

  @Test
  fun `fails if several initializers exist without annotation`() {
    assertThatThrownBy {
      ProcessTestInitializerResolver.resolve(
        PlainTest::class.java,
        listOf(FakeInitializer("c7remote"), FakeInitializer("c8")),
      )
    }
      .isInstanceOf(ProcessTestInitializerResolutionException::class.java)
      .hasMessageContaining("Multiple process test initializers were discovered")
      .hasMessageContaining("'c7remote'")
      .hasMessageContaining("'c8'")
  }

  @UseProcessTestInitializer("c7remote")
  abstract class AnnotatedBaseTest

  class InheritingChildTest : AnnotatedBaseTest()

  @UseProcessTestInitializer("c8")
  class AnnotatedChildTest : AnnotatedBaseTest()

  @UseProcessTestInitializer(
    qualifier = "c8",
    configuration = ["c8.deployment-propagation-delay=PT2S"],
  )
  class ConfiguredTest

  @UseProcessTestInitializer(
    qualifier = "c8",
    configuration = [
      "spring.profiles.active=process-test",
      "adapter.endpoint=http://test-override",
    ],
    propertyProviders = [ProfileAwarePropertiesProvider::class],
  )
  class PropertyProvidedTest

  class ProfileAwarePropertiesProvider : ProcessTestPropertyProvider {
    override fun load(request: ProcessTestPropertyRequest): Map<String, String> {
      lastRequest = request
      return mapOf(
        "adapter.endpoint" to "http://provider-default",
        "adapter.loaded-from-profile" to requireNotNull(request.explicitConfiguration["spring.profiles.active"]),
      )
    }

    companion object {
      var lastRequest: ProcessTestPropertyRequest? = null
    }
  }

  class PlainTest

  private class FakeInitializer(
    private val qualifier: String,
  ) : ProcessTestInitializer {

    override fun qualifier(): String = qualifier

    override fun initialize(request: ProcessTestInitializationRequest): ProcessTestContext {
      throw UnsupportedOperationException("Not needed in this test")
    }
  }
}
