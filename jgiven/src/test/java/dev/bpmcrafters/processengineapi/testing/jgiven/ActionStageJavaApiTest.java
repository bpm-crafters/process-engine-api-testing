package dev.bpmcrafters.processengineapi.testing.jgiven;

import dev.bpmcrafters.processengineapi.deploy.DeployBundleCommand;
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi;
import dev.bpmcrafters.processengineapi.deploy.DeploymentInformation;
import dev.bpmcrafters.processengineapi.task.CompleteTaskByErrorCmd;
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestAssertApi;
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestContext;
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestQueryApi;
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestRuntimeApis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.Map.of;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActionStageJavaApiTest {

  private ProcessTestContext context;
  private ProcessTestRuntimeApis runtimeApis;
  private DeploymentApi deploymentApi;
  private DeploymentInformation deployment;
  private ProcessTestAssertApi assertApi;
  private ProcessTestQueryApi queryApi;
  private ServiceTaskCompletionApi serviceTaskCompletionApi;
  private TaskInformation task;

  @BeforeEach
  void setUp() {
    context = mock(ProcessTestContext.class);
    runtimeApis = mock(ProcessTestRuntimeApis.class);
    deploymentApi = mock(DeploymentApi.class);
    deployment = mock(DeploymentInformation.class);
    assertApi = mock(ProcessTestAssertApi.class);
    queryApi = mock(ProcessTestQueryApi.class);
    serviceTaskCompletionApi = mock(ServiceTaskCompletionApi.class);
    task = new TaskInformation("external-task-id", Map.of());

    when(context.runtime()).thenReturn(runtimeApis);
    when(runtimeApis.deploymentApi()).thenReturn(deploymentApi);
    when(deploymentApi.deploy(any(DeployBundleCommand.class)))
      .thenReturn(CompletableFuture.completedFuture(deployment));
    when(context.assertions()).thenReturn(assertApi);
    when(context.query()).thenReturn(queryApi);
    when(runtimeApis.serviceTaskCompletionApi()).thenReturn(serviceTaskCompletionApi);
    when(assertApi.userTaskIsSelected("approve", Map.of(), null)).thenReturn(task);
    when(assertApi.externalTaskIsSelected("validate", Map.of(), null)).thenReturn(task);
    when(serviceTaskCompletionApi.completeTaskByError(any(CompleteTaskByErrorCmd.class)))
      .thenReturn(CompletableFuture.completedFuture(null));
  }

  @Test
  void deploymentStepDelegatesToTheRuntimeApiAndReturnsTheConcreteJavaStage() {
    JavaActionStage stage = new JavaActionStage();
    stage.processTestContext(context);

    JavaActionStage returnedStage = stage.processIsDeployed("processes/order.bpmn");

    assertSame(stage, returnedStage);
    verify(context).runtime();
    verify(runtimeApis).deploymentApi();
    verify(deploymentApi).deploy(any(DeployBundleCommand.class));
    verifyNoMoreInteractions(context, runtimeApis, deploymentApi);
  }

  @Test
  void selectionStepDelegatesToTheAssertApiAndReturnsTheConcreteJavaStage() {
    JavaAssertStage stage = new JavaAssertStage();
    stage.processTestContext(context);

    JavaAssertStage returnedStage = stage.userTaskIsSelected("approve");

    assertSame(stage, returnedStage);
    verify(context).assertions();
    verify(assertApi).userTaskIsSelected("approve", of(), null);
    verifyNoMoreInteractions(context, assertApi);
  }

  @Test
  void bpmnErrorCompletionDelegatesToTheRuntimeApiAndRefreshesTheCurrentProcess() {
    JavaActionStage stage = new JavaActionStage();
    stage.processTestContext(context);
    stage.selectExternalTask("instance-id", task);

    JavaActionStage returnedStage = stage.externalTaskIsCompletedWithError(
      "BUSINESS_ERROR", Map.of("reason", "invalid"), "Business validation failed"
    );

    assertSame(stage, returnedStage);
    verify(serviceTaskCompletionApi).completeTaskByError(argThat(command ->
      command.getTaskId().equals("external-task-id")
        && command.getErrorCode().equals("BUSINESS_ERROR")
        && command.getErrorMessage().equals("Business validation failed")
        && command.get().equals(Map.of("reason", "invalid"))
    ));
    verify(queryApi).getProcessInformation("instance-id");
  }

  @Test
  void bpmnErrorCompletionCanSelectTheExternalTaskBeforeCompletingIt() {
    JavaActionStage stage = new JavaActionStage();
    stage.processTestContext(context);
    stage.selectCurrentProcess("instance-id");

    stage.externalTaskIsCompletedWithError("validate", "BUSINESS_ERROR");

    verify(assertApi).externalTaskIsSelected("validate", Map.of(), null);
    verify(serviceTaskCompletionApi).completeTaskByError(argThat(command ->
      command.getTaskId().equals("external-task-id")
        && command.getErrorCode().equals("BUSINESS_ERROR")
        && command.getErrorMessage() == null
        && command.get().isEmpty()
    ));
  }

  private static final class JavaActionStage extends ActionStage<JavaActionStage> {
    void selectExternalTask(String instanceId, TaskInformation task) {
      selectCurrentProcess(instanceId);
      this.taskInformation = task;
    }

    void selectCurrentProcess(String instanceId) {
      this.instanceId = instanceId;
    }
  }

  private static final class JavaAssertStage extends AssertStage<JavaAssertStage> {
  }
}
