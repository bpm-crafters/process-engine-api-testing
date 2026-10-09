package dev.bpmcrafters.processengineapi.testing.jgiven;

import dev.bpmcrafters.processengineapi.Empty;
import dev.bpmcrafters.processengineapi.deploy.DeployBundleCommand;
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi;
import dev.bpmcrafters.processengineapi.deploy.DeploymentInformation;
import dev.bpmcrafters.processengineapi.process.ProcessInformation;
import dev.bpmcrafters.processengineapi.process.StartProcessApi;
import dev.bpmcrafters.processengineapi.process.StartProcessCommand;
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd;
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi;
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestAssertApi;
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestCapabilities;
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestContext;
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestQueryApi;
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestRuntimeApis;
import dev.bpmcrafters.processengineapi.testing.bootstrap.ProcessTestInitializationRequest;
import dev.bpmcrafters.processengineapi.testing.bootstrap.ProcessTestInitializer;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Test-scoped initializer that supplies a mock process-test context through ServiceLoader. */
public final class MockProcessTestInitializer implements ProcessTestInitializer {

  public static final String QUALIFIER = "mock";
  private static ScenarioMocks lastScenario;

  @Override
  public String qualifier() {
    return QUALIFIER;
  }

  @Override
  public ProcessTestContext initialize(ProcessTestInitializationRequest request) {
    ProcessTestContext context = mock(ProcessTestContext.class);
    ProcessTestRuntimeApis runtimeApis = mock(ProcessTestRuntimeApis.class);
    DeploymentApi deploymentApi = mock(DeploymentApi.class);
    StartProcessApi startProcessApi = mock(StartProcessApi.class);
    ServiceTaskCompletionApi serviceTaskCompletionApi = mock(ServiceTaskCompletionApi.class);
    UserTaskCompletionApi userTaskCompletionApi = mock(UserTaskCompletionApi.class);
    ProcessTestAssertApi assertApi = mock(ProcessTestAssertApi.class);
    ProcessTestQueryApi queryApi = mock(ProcessTestQueryApi.class);
    ProcessTestCapabilities capabilities = mock(ProcessTestCapabilities.class);
    ProcessInformation process = new ProcessInformation("order-1", Map.of());
    TaskInformation serviceTask = new TaskInformation("service-task-1", Map.of());
    TaskInformation userTask = new TaskInformation("user-task-1", Map.of());

    when(context.runtime()).thenReturn(runtimeApis);
    when(context.assertions()).thenReturn(assertApi);
    when(context.query()).thenReturn(queryApi);
    when(context.capabilities()).thenReturn(capabilities);
    when(runtimeApis.deploymentApi()).thenReturn(deploymentApi);
    when(runtimeApis.startProcessApi()).thenReturn(startProcessApi);
    when(runtimeApis.serviceTaskCompletionApi()).thenReturn(serviceTaskCompletionApi);
    when(runtimeApis.userTaskCompletionApi()).thenReturn(userTaskCompletionApi);
    when(deploymentApi.deploy(any(DeployBundleCommand.class)))
      .thenReturn(CompletableFuture.completedFuture(mock(DeploymentInformation.class)));
    when(startProcessApi.startProcess(any(StartProcessCommand.class)))
      .thenReturn(CompletableFuture.completedFuture(process));
    when(serviceTaskCompletionApi.completeTask(any(CompleteTaskCmd.class)))
      .thenReturn(CompletableFuture.completedFuture(Empty.INSTANCE));
    when(userTaskCompletionApi.completeTask(any(CompleteTaskCmd.class)))
      .thenReturn(CompletableFuture.completedFuture(Empty.INSTANCE));
    when(assertApi.externalTaskIsSelected("validateOrder", Map.of(), null)).thenReturn(serviceTask);
    when(assertApi.userTaskIsSelected("approveOrder", Map.of(), null)).thenReturn(userTask);
    when(assertApi.processWaitsInUserTask("order-1", "approveOrder", Map.of(), null)).thenReturn(userTask);
    when(queryApi.getProcessInformation("order-1")).thenReturn(process);
    lastScenario = new ScenarioMocks(
      context,
      runtimeApis,
      deploymentApi,
      startProcessApi,
      serviceTaskCompletionApi,
      userTaskCompletionApi,
      assertApi
    );
    return context;
  }

  static ScenarioMocks lastScenario() {
    return lastScenario;
  }

  static final class ScenarioMocks {
    final ProcessTestContext context;
    final ProcessTestRuntimeApis runtimeApis;
    final DeploymentApi deploymentApi;
    final StartProcessApi startProcessApi;
    final ServiceTaskCompletionApi serviceTaskCompletionApi;
    final UserTaskCompletionApi userTaskCompletionApi;
    final ProcessTestAssertApi assertApi;

    ScenarioMocks(
      ProcessTestContext context,
      ProcessTestRuntimeApis runtimeApis,
      DeploymentApi deploymentApi,
      StartProcessApi startProcessApi,
      ServiceTaskCompletionApi serviceTaskCompletionApi,
      UserTaskCompletionApi userTaskCompletionApi,
      ProcessTestAssertApi assertApi
    ) {
      this.context = context;
      this.runtimeApis = runtimeApis;
      this.deploymentApi = deploymentApi;
      this.startProcessApi = startProcessApi;
      this.serviceTaskCompletionApi = serviceTaskCompletionApi;
      this.userTaskCompletionApi = userTaskCompletionApi;
      this.assertApi = assertApi;
    }
  }
}
