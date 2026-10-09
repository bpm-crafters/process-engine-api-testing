package dev.bpmcrafters.processengineapi.testing.jgiven

import com.tngtech.jgiven.base.ScenarioTestBase

/**
 * A reusable, receiver-based group of operations for one JGiven stage.
 */
typealias Step<STAGE> = STAGE.() -> Unit

/**
 * Names a receiver-based [steps] block for reuse in a scenario.
 *
 * For example: `val deploy: Step<MyActionStage> = step { processIsDeployed("process.bpmn") }`.
 * @param steps receiver-based operations to reuse for one stage type.
 * @return the supplied receiver-based step unchanged.
 */
fun <STAGE> step(steps: Step<STAGE>): Step<STAGE> = steps

/**
 * Returns this scenario's When stage using a Kotlin-safe name.
 * @receiver JGiven scenario whose When stage is requested.
 * @return the scenario's configured When stage.
 */
fun <GIVEN, WHEN, THEN> ScenarioTestBase<GIVEN, WHEN, THEN>.whenever(): WHEN = this.`when`()

/**
 * Executes [steps] against this scenario's Given stage.
 *
 * This Kotlin convenience overload permits `given { ... }` while retaining JGiven's scenario
 * state handling and report generation.
 *
 * @param steps Given-stage operations for the scenario
 * @return no value; all operations are executed against the Given stage.
 */
inline fun <GIVEN, WHEN, THEN> ScenarioTestBase<GIVEN, WHEN, THEN>.given(
  steps: Step<GIVEN>,
) {
  given().steps()
}

/**
 * Executes [steps] against this scenario's When stage.
 *
 * @param steps When-stage operations for the scenario
 * @return no value; all operations are executed against the When stage.
 */
inline fun <GIVEN, WHEN, THEN> ScenarioTestBase<GIVEN, WHEN, THEN>.whenever(
  steps: Step<WHEN>,
) {
  whenever().steps()
}

/**
 * Executes [steps] against this scenario's Then stage.
 *
 * @param steps Then-stage operations for the scenario
 * @return no value; all operations are executed against the Then stage.
 */
inline fun <GIVEN, WHEN, THEN> ScenarioTestBase<GIVEN, WHEN, THEN>.then(
  steps: Step<THEN>,
) {
  then().steps()
}
