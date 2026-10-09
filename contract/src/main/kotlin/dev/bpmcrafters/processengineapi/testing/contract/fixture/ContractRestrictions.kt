package dev.bpmcrafters.processengineapi.testing.contract.fixture

import dev.bpmcrafters.processengineapi.CommonRestrictions.builder

/**
 * Returns restrictions selecting this fixture's process definition.
 *
 * @return restrictions scoped to [ProcessModelFixture.definitionKey]
 */
fun ProcessModelFixture.definitionRestrictions(): Map<String, String> =
  builder()
    .withProcessDefinitionKey(definitionKey())
    .build()

/**
 * Returns restrictions selecting [activityId] in this fixture's process definition.
 *
 * @param activityId BPMN activity identifier used to disambiguate a task or wait state
 * @return restrictions scoped to this fixture's process definition and [activityId]
 */
fun ProcessModelFixture.activityRestrictions(activityId: String): Map<String, String> =
  builder()
    .withProcessDefinitionKey(definitionKey())
    .withActivityId(activityId)
    .build()
