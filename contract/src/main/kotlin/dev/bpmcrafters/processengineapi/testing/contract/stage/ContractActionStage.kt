package dev.bpmcrafters.processengineapi.testing.contract.stage

import dev.bpmcrafters.processengineapi.testing.jgiven.ActionStage

/**
 * Concrete engine-agnostic action stage used by the compatibility suites.
 *
 * Adapter runners should use this stage directly and add no engine-specific action methods to the
 * shared contract scenarios.
 */
open class ContractActionStage : ActionStage<ContractActionStage>()
