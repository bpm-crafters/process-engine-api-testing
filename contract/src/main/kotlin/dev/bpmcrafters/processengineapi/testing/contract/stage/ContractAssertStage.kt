package dev.bpmcrafters.processengineapi.testing.contract.stage

import dev.bpmcrafters.processengineapi.testing.jgiven.AssertStage

/**
 * Concrete engine-agnostic assertion stage used by the compatibility suites.
 *
 * Adapter runners should use this stage directly and add no engine-specific assertion methods to
 * the shared contract scenarios.
 */
open class ContractAssertStage : AssertStage<ContractAssertStage>()
