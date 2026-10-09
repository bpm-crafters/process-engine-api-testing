package dev.bpmcrafters.processengineapi.testing.jgiven;

import dev.bpmcrafters.processengineapi.deploy.DeployBundleCommand;
import dev.bpmcrafters.processengineapi.process.StartProcessCommand;
import dev.bpmcrafters.processengineapi.task.CompleteTaskCmd;
import dev.bpmcrafters.processengineapi.testing.config.UseProcessTestInitializer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Exercises the public JGiven process-test API exactly as a Java customer test would.
 */
@UseProcessTestInitializer(qualifier = MockProcessTestInitializer.QUALIFIER)
class JavaProcessScenarioTest extends ProcessScenarioTest<
  JavaProcessScenarioTest.JavaActionStage,
  JavaProcessScenarioTest.JavaAssertStage> {

  @Test
  void startsAtTheServiceTask() {

    given()
      .processIsDeployed("processes/order.bpmn");

    when()
      .processIsStartedByDefinition("order");

    then()
      .processHasPassed("start")
      .processWaitsInElement("validateOrder");

    MockProcessTestInitializer.ScenarioMocks mocks = MockProcessTestInitializer.lastScenario();
    verify(mocks.deploymentApi).deploy(any(DeployBundleCommand.class));
    verify(mocks.startProcessApi).startProcess(any(StartProcessCommand.class));
    verify(mocks.assertApi).processHasPassed(eq("order-1"), eq(Map.of()), eq("start"));
    verify(mocks.assertApi).processWaitsInElement(eq("order-1"), eq("validateOrder"), eq(Map.of()));
  }

  @Test
  void completesTheServiceTaskAndWaitsForApproval() {
    given()
      .processIsDeployed("processes/order.bpmn");

    when()
      .processIsStartedByDefinition("order")
      .externalTaskIsCompleted("validateOrder");

    then()
      .processWaitsInUserTask("approveOrder")
      .processHasPassed("validateOrder");

    MockProcessTestInitializer.ScenarioMocks mocks = MockProcessTestInitializer.lastScenario();
    verify(mocks.serviceTaskCompletionApi).completeTask(any(CompleteTaskCmd.class));
    verify(mocks.assertApi).processWaitsInUserTask(eq("order-1"), eq("approveOrder"), eq(Map.of()), eq(null));
    verify(mocks.assertApi).processHasPassed(eq("order-1"), eq(Map.of()), eq("validateOrder"));
  }

  @Test
  void completesTheApprovalAndFinishesTheProcess() {
    given()
      .processIsDeployed("processes/order.bpmn");

    when()
      .processIsStartedByDefinition("order")
      .userTaskIsCompleted("approveOrder");

    then()
      .processHasPassed("approveOrder", "end")
      .processIsFinished();

    MockProcessTestInitializer.ScenarioMocks mocks = MockProcessTestInitializer.lastScenario();
    verify(mocks.userTaskCompletionApi).completeTask(any(CompleteTaskCmd.class));
    verify(mocks.assertApi).processHasPassed(eq("order-1"), eq(Map.of()), eq("approveOrder"), eq("end"));
    verify(mocks.assertApi).processIsFinished(eq("order-1"), eq(Map.of()));
  }

  public static class JavaActionStage extends ActionStage<JavaActionStage> {
  }

  public static class JavaAssertStage extends AssertStage<JavaAssertStage> {
  }
}
