---
title: Process-testing quick start
---

# Process-testing quick start

Add `process-engine-api-testing-jgiven` and the testing artifact contributed by your adapter as
test dependencies. The adapter pages list exact coordinates and environment requirements.

```xml
<dependencies>
  <dependency>
    <groupId>dev.bpm-crafters.process-engine-api-testing</groupId>
    <artifactId>process-engine-api-testing-jgiven</artifactId>
    <version>${process-engine-api-testing.version}</version>
    <scope>test</scope>
  </dependency>

  <!-- Replace with the testing provider for the selected process engine. -->
  <dependency>
    <groupId>dev.bpm-crafters.process-engine-adapters</groupId>
    <artifactId>process-engine-adapter-...-testing</artifactId>
    <version>${process-engine-adapter.version}</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```

=== "Kotlin"

    ```kotlin
    @UseProcessTestInitializer(MyAdapterProcessTestInitializer.QUALIFIER)
    class OrderTest : ProcessScenarioTest<OrderActions, OrderAssertions>() {
      @Test fun `order follows its expected path`() {
        given {
          // Arrange the required process state.
        }
        whenever {
          // Trigger the business event.
        }
        then {
          // Verify the expected process state.
        }
      }
    }
    open class OrderActions : ActionStage<OrderActions>()
    open class OrderAssertions : AssertStage<OrderAssertions>()
    ```

=== "Java"

    ```java
    @UseProcessTestInitializer(MyAdapterProcessTestInitializer.QUALIFIER)
    class OrderTest extends ProcessScenarioTest<OrderActions, OrderAssertions> {
      @Test
      void orderFollowsItsExpectedPath() {
        given()
          // Arrange the required process state.
        ; 
        when()
          // Trigger the business event.
        ;  
        then()
          // Verify the expected process state.
        ;  
      }
    }
    class OrderActions extends ActionStage<OrderActions> {}
    class OrderAssertions extends AssertStage<OrderAssertions> {}
    ```

If exactly one initializer is available, the annotation is optional. It is required to select one
among multiple integrations and can pass adapter-specific `key=value` configuration. Start with a
scenario that deploys a model, starts it, and asserts its first wait state; then grow the suite
along the process control flow.
