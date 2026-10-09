package dev.bpmcrafters.processengineapi.testing.jgiven;

import dev.bpmcrafters.processengineapi.deploy.DeployBundleCommand;
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi;
import dev.bpmcrafters.processengineapi.deploy.DeploymentInformation;
import dev.bpmcrafters.processengineapi.task.TaskInformation;
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestAssertApi;
import dev.bpmcrafters.processengineapi.testing.api.ProcessTestContext;
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
  private TaskInformation task;

  @BeforeEach
  void setUp() {
    context = mock(ProcessTestContext.class);
    runtimeApis = mock(ProcessTestRuntimeApis.class);
    deploymentApi = mock(DeploymentApi.class);
    deployment = mock(DeploymentInformation.class);
    assertApi = mock(ProcessTestAssertApi.class);
    task = mock(TaskInformation.class);

    when(context.runtime()).thenReturn(runtimeApis);
    when(runtimeApis.deploymentApi()).thenReturn(deploymentApi);
    when(deploymentApi.deploy(any(DeployBundleCommand.class)))
      .thenReturn(CompletableFuture.completedFuture(deployment));
    when(context.assertions()).thenReturn(assertApi);
    when(assertApi.userTaskIsSelected("approve", Map.of(), null)).thenReturn(task);
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

  private static final class JavaActionStage extends ActionStage<JavaActionStage> {
  }

  private static final class JavaAssertStage extends AssertStage<JavaAssertStage> {
  }
}
