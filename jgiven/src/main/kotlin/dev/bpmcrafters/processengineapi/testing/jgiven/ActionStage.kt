package dev.bpmcrafters.processengineapi.testing.jgiven

import com.tngtech.jgiven.annotation.Description
import com.tngtech.jgiven.annotation.Hidden
import com.tngtech.jgiven.annotation.Quoted
import dev.bpmcrafters.processengineapi.CommonRestrictions.builder
import dev.bpmcrafters.processengineapi.correlation.CorrelateMessageCmd
import dev.bpmcrafters.processengineapi.correlation.Correlation
import dev.bpmcrafters.processengineapi.correlation.SendSignalCmd
import dev.bpmcrafters.processengineapi.deploy.DeployBundleCommand
import dev.bpmcrafters.processengineapi.deploy.NamedResource.Companion.fromClasspath
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionAtElementCmd
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionCmd
import dev.bpmcrafters.processengineapi.process.StartProcessByMessageCmd
import dev.bpmcrafters.processengineapi.task.ChangeAssignmentModifyTaskCmd.AssignTaskCmd
import dev.bpmcrafters.processengineapi.task.ChangePayloadModifyTaskCmd.UpdatePayloadTaskCmd
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd
import dev.bpmcrafters.processengineapi.task.FailTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskInformation
import java.time.Duration
import java.util.function.Predicate

/**
 * JGiven base stage providing engine-agnostic process actions.
 */
abstract class ActionStage<SUBTYPE : ActionStage<SUBTYPE>> : ProcessStage<SUBTYPE>() {
  /**
   * Deploys one classpath resource for the current scenario.
   * @param classpathResource classpath location of the process resource.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @Description("process is deployed from \$classpathResource")
  open fun processIsDeployed(@Quoted classpathResource: String): SUBTYPE = step {
    processIsDeployed(DeployBundleCommand(resources = listOf(fromClasspath(classpathResource))))
  }

  /**
   * Deploys all resources in a bundle.
   * @param deployBundleCommand deployment command to submit.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @Description("process bundle \$deployBundleCommand is deployed")
  open fun processIsDeployed(deployBundleCommand: DeployBundleCommand): SUBTYPE = step {
    runtimeApis().deploymentApi().deploy(deployBundleCommand).get()
  }

  /**
   * Starts a process definition and makes its instance current.
   * @param definitionKey definition key to start.
   * @param payload process payload supplied at start.
   * @param restrictions definition-resolution restrictions.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @JvmOverloads
  @Description("process \$definitionKey is started by definition")
  open fun processIsStartedByDefinition(
    @Quoted definitionKey: String,
    @Hidden payload: Map<String, Any?> = emptyMap(),
    @Hidden restrictions: Map<String, String> = builder().build(),
  ): SUBTYPE = step {
    val startedProcess = runtimeApis().startProcessApi().startProcess(
      StartProcessByDefinitionCmd(definitionKey, { payload }, restrictions)
    ).get()
    instanceId = startedProcess.instanceId
    processInformation = startedProcess
  }

  /**
   * Starts a process definition at a specific BPMN element.
   * @param definitionKey definition key to start.
   * @param elementId BPMN element at which execution starts.
   * @param payload process payload supplied at start.
   * @param restrictions definition-resolution restrictions.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @JvmOverloads
  @Description("process \$definitionKey is started at \$elementId")
  open fun processIsStartedByDefinitionAtElement(
    @Quoted definitionKey: String,
    @Quoted elementId: String,
    @Hidden payload: Map<String, Any?> = emptyMap(),
    @Hidden restrictions: Map<String, String> = builder().build(),
  ): SUBTYPE = step {
    val startedProcess = runtimeApis().startProcessApi().startProcess(
      StartProcessByDefinitionAtElementCmd(definitionKey, elementId, { payload }, restrictions)
    ).get()
    instanceId = startedProcess.instanceId
    processInformation = startedProcess
  }

  /**
   * Starts a process by correlating a start message.
   * @param messageName BPMN message name.
   * @param payload process payload supplied with the message.
   * @param restrictions correlation restrictions.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @JvmOverloads
  @Description("process is started by message \$messageName")
  open fun processIsStartedByMessage(
    @Quoted messageName: String,
    @Hidden payload: Map<String, Any?> = emptyMap(),
    @Hidden restrictions: Map<String, String> = builder().build(),
  ): SUBTYPE = step {
    val startedProcess = runtimeApis().startProcessApi().startProcess(
      StartProcessByMessageCmd(messageName, { payload }, restrictions)
    ).get()
    instanceId = startedProcess.instanceId
    processInformation = startedProcess
  }

  /**
   * Correlates a message with a running process.
   * @param messageName BPMN message name.
   * @param correlation correlation criteria identifying the receiver.
   * @param payload payload supplied with the message.
   * @param restrictions correlation restrictions.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @JvmOverloads
  @Description("message \$messageName is correlated with \$correlation")
  open fun messageIsCorrelated(
    @Quoted messageName: String,
    correlation: Correlation,
    @Hidden payload: Map<String, Any?> = emptyMap(),
    @Hidden restrictions: Map<String, String> = builder().build(),
  ): SUBTYPE = step {
    runtimeApis().correlationApi().correlateMessage(
      CorrelateMessageCmd(messageName, payload, correlation, restrictions)
    ).get()
    refreshProcessInformation()
  }

  /**
   * Sends a BPMN signal.
   * @param signalName BPMN signal name.
   * @param payload payload supplied with the signal.
   * @param restrictions signal-delivery restrictions.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @JvmOverloads
  @Description("signal \$signalName is sent")
  open fun signalIsSent(
    @Quoted signalName: String,
    @Hidden payload: Map<String, Any?> = emptyMap(),
    @Hidden restrictions: Map<String, String> = builder().build(),
  ): SUBTYPE = step {
    runtimeApis().signalApi().sendSignal(SendSignalCmd(signalName, restrictions, payload)).get()
    refreshProcessInformation()
  }

  /**
   * Completes the currently selected user task.
   * @param payload task payload supplied on completion.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @Description("selected user task is completed")
  open fun userTaskIsCompleted(@Hidden payload: Map<String, Any?> = emptyMap()): SUBTYPE = step {
    runtimeApis().userTaskCompletionApi().completeTask(
      CompleteTaskCmd(requireCurrentTask().taskId) { payload }
    ).get()
    refreshProcessInformation()
  }

  /**
   * Selects and completes one matching user task.
   * @param taskDescriptionKey optional user-task description key.
   * @param payload task payload supplied on completion.
   * @param restrictions task-selection restrictions.
   * @param predicate optional post-restriction task filter.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @JvmOverloads
  @Description("user task \$taskDescriptionKey is completed")
  open fun userTaskIsCompleted(
    @Quoted taskDescriptionKey: String? = null,
    @Hidden payload: Map<String, Any?> = emptyMap(),
    @Hidden restrictions: Map<String, String> = builder().build(),
    @Hidden predicate: Predicate<TaskInformation>? = null,
  ): SUBTYPE = step {
    userTaskIsSelected(taskDescriptionKey, restrictions, predicate)
    userTaskIsCompleted(payload)
  }

  /**
   * Assigns the selected user task.
   * @param assignee user identifier to assign.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @Description("selected user task is assigned to \$assignee")
  open fun selectedUserTaskIsAssignedTo(@Quoted assignee: String): SUBTYPE = step {
    runtimeApis().userTaskModificationApi().update(AssignTaskCmd(requireCurrentTask().taskId, assignee)).get()
  }

  /**
   * Updates payload values of the current process.
   * @param payload variable names and values to set.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @Description("current process payload is updated")
  open fun currentProcessPayloadIsUpdated(@Hidden payload: Map<String, Any?>): SUBTYPE = step {
    payloadApi().updateProcessPayload(requireInstanceId(), payload)
    refreshProcessInformation()
  }

  /**
   * Updates local payload values of the selected user task.
   * @param payload variable names and values to set.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @Description("selected user task payload is updated")
  open fun selectedUserTaskPayloadIsUpdated(@Hidden payload: Map<String, Any?>): SUBTYPE = step {
    runtimeApis().userTaskModificationApi().update(UpdatePayloadTaskCmd(requireCurrentTask().taskId, payload)).get()
  }

  /**
   * Completes the currently selected external task.
   * @param payload task payload supplied on completion.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @Description("selected external task is completed")
  open fun externalTaskIsCompleted(@Hidden payload: Map<String, Any?> = emptyMap()): SUBTYPE = step {
    runtimeApis().serviceTaskCompletionApi().completeTask(
      CompleteTaskCmd(requireCurrentTask().taskId) { payload }
    ).get()
    refreshProcessInformation()
  }

  /**
   * Selects and completes one matching external task.
   * @param taskDescriptionKey optional external-task description key.
   * @param payload task payload supplied on completion.
   * @param restrictions task-selection restrictions.
   * @param predicate optional post-restriction task filter.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @JvmOverloads
  @Description("external task \$taskDescriptionKey is completed")
  open fun externalTaskIsCompleted(
    @Quoted taskDescriptionKey: String? = null,
    @Hidden payload: Map<String, Any?> = emptyMap(),
    @Hidden restrictions: Map<String, String> = builder().build(),
    @Hidden predicate: Predicate<TaskInformation>? = null,
  ): SUBTYPE = step {
    externalTaskIsSelected(taskDescriptionKey, restrictions, predicate)
    externalTaskIsCompleted(payload)
  }

  /**
   * Fails the selected external task.
   * @param reason engine-visible failure reason.
   * @param retries retry count remaining after the failure.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @Description("selected external task fails with \$reason and \$retries retries")
  open fun externalTaskHasFailed(@Quoted reason: String, retries: Int): SUBTYPE = step {
    runtimeApis().serviceTaskCompletionApi().failTask(
      FailTaskCmd(requireCurrentTask().taskId, reason, null, retries, Duration.ofSeconds(3))
    ).get()
    refreshProcessInformation()
  }

  /**
   * Advances engine time when the configured context supports it.
   * @param duration duration by which to advance engine time.
   * @return this concrete action stage for Java/JGiven chaining.
   */
  @Description("time passes by \$duration")
  open fun timePasses(duration: Duration): SUBTYPE = step {
    require(capabilities().supportsTimeTravel()) {
      "Selected process test initializer '${processTestContext.qualifier()}' does not support time travel."
    }
    capabilities().timePasses(duration)
    refreshProcessInformation()
  }

  protected fun captureException(action: Runnable): Throwable? {
    throwableCaught = try {
      action.run()
      null
    } catch (throwable: Throwable) {
      throwable
    }
    return throwableCaught
  }
}
