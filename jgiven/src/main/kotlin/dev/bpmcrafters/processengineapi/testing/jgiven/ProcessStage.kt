package dev.bpmcrafters.processengineapi.testing.jgiven

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.Description
import com.tngtech.jgiven.annotation.ExpectedScenarioState
import com.tngtech.jgiven.annotation.Hidden
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.annotation.Quoted
import dev.bpmcrafters.processengineapi.CommonRestrictions.builder
import dev.bpmcrafters.processengineapi.process.ProcessInformation
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestAssertApi
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestCapabilities
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestContext
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestPayloadApi
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestQueryApi
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestRuntimeApis
import java.util.function.Predicate

/**
 * Common JGiven support for process stages. It owns scenario state and operational selection and
 * synchronization steps; outcome assertions belong exclusively to [AssertStage].
 */
abstract class ProcessStage<SUBTYPE : ProcessStage<SUBTYPE>> : Stage<SUBTYPE>() {

  @ExpectedScenarioState
  protected lateinit var processTestContext: ProcessTestContext

  /**
   * Supplies the process-test context for this scenario stage.
   * @param processTestContext initialized context to use for all stage operations.
   * @return no value; JGiven injects this lifecycle state.
   */
  fun processTestContext(processTestContext: ProcessTestContext) {
    this.processTestContext = processTestContext
  }

  @ProvidedScenarioState
  protected lateinit var instanceId: String

  @ProvidedScenarioState
  protected lateinit var processInformation: ProcessInformation

  @ProvidedScenarioState
  protected lateinit var taskInformation: TaskInformation

  @ProvidedScenarioState
  protected var throwableCaught: Throwable? = null

  protected fun runtimeApis(): ProcessTestRuntimeApis = processTestContext.runtime()
  protected fun queryApi(): ProcessTestQueryApi = processTestContext.query()
  protected fun assertApi(): ProcessTestAssertApi = processTestContext.assertions()
  protected fun payloadApi(): ProcessTestPayloadApi = processTestContext.payloads()
  protected fun capabilities(): ProcessTestCapabilities = processTestContext.capabilities()

  /** Executes one Java/JGiven-visible stage operation and returns the concrete stage for chaining. */
  protected inline fun step(action: () -> Unit): SUBTYPE {
    action()
    return self()
  }

  /**
   * Selects exactly one user task for a later action or assertion.
   * @param taskDescriptionKey optional user-task description key.
   * @param restrictions task-selection restrictions.
   * @param predicate optional post-restriction task filter.
   * @return this concrete stage for Java/JGiven chaining.
   */
  @JvmOverloads
  @Description("user task \$taskDescriptionKey is selected")
  open fun userTaskIsSelected(
    @Quoted taskDescriptionKey: String? = null,
    @Hidden restrictions: Map<String, String> = builder().build(),
    @Hidden predicate: Predicate<TaskInformation>? = null,
  ): SUBTYPE = step {
    taskInformation = assertApi().userTaskIsSelected(taskDescriptionKey, restrictions, predicate)
  }

  /**
   * Selects exactly one external task for a later action or assertion.
   * @param taskDescriptionKey optional external-task description key.
   * @param restrictions task-selection restrictions.
   * @param predicate optional post-restriction task filter.
   * @return this concrete stage for Java/JGiven chaining.
   */
  @JvmOverloads
  @Description("external task \$taskDescriptionKey is selected")
  open fun externalTaskIsSelected(
    @Quoted taskDescriptionKey: String? = null,
    @Hidden restrictions: Map<String, String> = builder().build(),
    @Hidden predicate: Predicate<TaskInformation>? = null,
  ): SUBTYPE = step {
    taskInformation = assertApi().externalTaskIsSelected(taskDescriptionKey, restrictions, predicate)
  }

  /**
   * Waits until the current process reaches one matching user task and selects it.
   * @param taskDescriptionKey optional user-task description key.
   * @param restrictions task-selection restrictions.
   * @param predicate optional post-restriction task filter.
   * @return this concrete stage for Java/JGiven chaining.
   */
  @JvmOverloads
  @Description("process waits in user task \$taskDescriptionKey")
  open fun processWaitsInUserTask(
    @Quoted taskDescriptionKey: String? = null,
    @Hidden restrictions: Map<String, String> = builder().build(),
    @Hidden predicate: Predicate<TaskInformation>? = null,
  ): SUBTYPE = step {
    taskInformation = assertApi().processWaitsInUserTask(requireInstanceId(), taskDescriptionKey, restrictions, predicate)
  }

  /**
   * Waits until the current process reaches a BPMN element.
   * @param elementId BPMN element identifier that must become active.
   * @param restrictions restrictions that scope the wait.
   * @return this concrete stage for Java/JGiven chaining.
   */
  @JvmOverloads
  @Description("process waits in element \$elementId")
  open fun processWaitsInElement(
    @Quoted elementId: String,
    @Hidden restrictions: Map<String, String> = builder().build(),
  ): SUBTYPE = step {
    assertApi().processWaitsInElement(requireInstanceId(), elementId, restrictions)
    refreshProcessInformation()
  }

  protected fun requireCurrentTask(): TaskInformation = requireNotNull(
    if (this::taskInformation.isInitialized) taskInformation else null
  ) {
    "No current task is selected. Use processWaitsInUserTask, userTaskIsSelected or externalTaskIsSelected first."
  }

  protected fun requireInstanceId(): String = requireNotNull(
    if (this::instanceId.isInitialized) instanceId else null
  ) { "No current process instance is selected. Start a process first." }

  protected fun refreshProcessInformation(): ProcessInformation? {
    val currentProcessInformation = queryApi().getProcessInformation(requireInstanceId())
    if (currentProcessInformation != null) {
      require(currentProcessInformation.instanceId == instanceId) {
        "ProcessInformation instance id ${currentProcessInformation.instanceId} diverges from current instance id $instanceId."
      }
      processInformation = currentProcessInformation
    }
    return currentProcessInformation
  }
}
