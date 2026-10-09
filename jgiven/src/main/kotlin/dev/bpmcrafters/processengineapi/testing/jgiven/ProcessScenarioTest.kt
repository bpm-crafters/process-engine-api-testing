package dev.bpmcrafters.processengineapi.testing.jgiven

import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.junit5.DualScenarioTest
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestContext
import org.junit.jupiter.api.extension.ExtendWith

/**
 * JUnit 5/JGiven base test that initializes one [ProcessTestContext] for each scenario.
 *
 * Use [dev.bpmcrafters.processengineapi.testing.config.UseProcessTestInitializer] when more than one
 * adapter initializer is available on the test class path.
 */
@ExtendWith(ProcessTestInitializerExtension::class)
abstract class ProcessScenarioTest<
  ACTION_STAGE : ActionStage<ACTION_STAGE>,
  ASSERT_STAGE : AssertStage<ASSERT_STAGE>
  > : DualScenarioTest<ACTION_STAGE, ASSERT_STAGE>(), ProcessTestContextAware {

  @ProvidedScenarioState
  protected lateinit var processTestContext: ProcessTestContext

  override fun processTestContext(processTestContext: ProcessTestContext) {
    this.processTestContext = processTestContext
    scenario.givenStage.processTestContext(processTestContext)
    scenario.thenStage.processTestContext(processTestContext)
  }
}
