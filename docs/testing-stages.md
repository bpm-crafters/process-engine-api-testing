---
title: Stages and domain abstractions
---

# Stages and domain abstractions

Stages turn generic testing operations into business language. `ActionStage` owns events such as
starts, completions, messages, and signals. `AssertStage` owns paths, completion, incidents,
payload, and metadata assertions. `ProcessStage` supplies shared state, selection, and waits.

=== "Kotlin"

    ```kotlin
    open class LoanActions : ActionStage<LoanActions>() {
      fun anApplicationIsSubmitted(amount: Int) = step {
        processIsStartedByDefinition("loan", mapOf("amount" to amount))
      }
    }
    ```

=== "Java"

    ```java
    class LoanActions extends ActionStage<LoanActions> {
      LoanActions anApplicationIsSubmitted(int amount) {
        return processIsStartedByDefinition("loan", Map.of("amount", amount));
      }
    }
    ```

Keep changes caused by a BPMN refactoring inside these composed steps. Advanced stages can access
the protected grouped context APIs, but use generic stage operations where possible to retain
adapter-neutral semantics.
