# Specification

## Status

Draft specification based on the completed design grilling session.

## 1. Objective

`process-engine-api-testing` provides a customer-facing, engine-agnostic testing API for process tests. It must allow one shared scenario
and one shared set of business stages to run unchanged across supported engines by replacing only engine/application drivers, runner setup,
and engine-specific BPMN resources.

## 2. Supported Testing Style

### 2.1 Primary style

The primary intended usage is JGiven-based BDD process testing.

### 2.2 Recommended layering

Two usage levels are supported:

- direct process-level tests using generic engine-agnostic verbs
- domain-level business tests implemented by extending the common stages and translating business actions into `process-engine-api`
  primitives

Domain-level expression is preferred, but low-level process assertions remain supported.

## 3. Product Topology

The testing feature is delivered as a new library family:

- `process-engine-api-testing-core`
- `process-engine-api-testing-jgiven`
- `process-engine-api-testing-contract`

Adapter-side support is published from adapter repositories as new customer-facing testing artifacts.

The old adapter-side `adapter.*.testing` public stages are expected to be removed as a breaking change after migration.

### 3.1 Public package layout

The testing core separates its public types by responsibility:

- `dev.bpmcrafters.processengineapi.testing.api`: grouped context and runtime, query, assertion, capability, and diagnostic contracts
- `dev.bpmcrafters.processengineapi.testing.config`: polling and initializer-selection configuration
- `dev.bpmcrafters.processengineapi.testing.bootstrap`: `ServiceLoader` initializer SPI and selection support
- `dev.bpmcrafters.processengineapi.testing.jgiven`: JGiven stages and JUnit integration

The bootstrap package is intentionally separate from configuration: an initializer creates engine test infrastructure and is not a
customer-configured builder.

### 3.2 Adapter compatibility contract

`process-engine-api-testing-contract` provides reusable abstract JUnit/JGiven suites that assert the
public behavior of the testing API. It depends only on the engine-agnostic testing artifacts and
`process-engine-api`. Its initial suite verifies the normal user-task path; additional suites are
added by feature area and capability.

An adapter consumes the contract as a test dependency and supplies a thin concrete runner that
selects an initializer, provides engine-specific BPMN resources, and maps logical process-model
names to the adapter's resources. Shared suites must not import raw engine types or adapter-private
test helpers. They are organized by feature area and optional capability so adapter adoption can be
incremental.

The contract ships C7-reference BPMN models under
`contract/src/main/resources/reference/c7`. Their BPMN text annotations identify the C7-specific
mechanism, such as external-task topics or clock control, and the adapter responsibility for mapping
it to an equivalent engine feature. They are design references, not shared deployable fixtures.

## 4. Public Dependency Boundary

Customer-facing shared test code must depend only on:

- `process-engine-api-testing-core`
- `process-engine-api-testing-jgiven`
- `process-engine-api`

Runner modules may additionally depend on one adapter-specific testing integration artifact.

Customer test source must not import:

- raw Camunda 7 classes
- raw Camunda 8 classes
- adapter-specific public testing stages

## 5. Public Runtime Model

### 5.1 Grouped context

One grouped `ProcessTestContext` is the public runtime root object. It is provided as one scenario-state object.

### 5.2 Internal partitioning

The grouped context exposes named sub-interfaces:

- runtime APIs
- query API
- assert API
- capabilities
- diagnostics
- payload mutation and snapshot API

### 5.3 Lifecycle

The grouped context is created externally by a selected initializer and has a standard cleanup contract.

## 6. Existing `process-engine-api` Reuse

The public testing model reuses existing `process-engine-api` contracts where possible:

- `StartProcessApi`
- `TaskSubscriptionApi`
- `UserTaskCompletionApi`
- `ServiceTaskCompletionApi`
- `CorrelationApi`
- `SignalApi`
- `DeploymentApi`
- existing commands such as `DeployBundleCommand`
- `TaskInformation`
- `ProcessInformation`

No new public task reference wrapper types are introduced in v1.

`TaskInformation` remains meta-driven in v1. The design does not introduce new explicit top-level fields just for testing.

## 7. Query and Assertion Model

### 7.1 Query API

The query API is read-only and snapshot-based.

It supports:

- lookup of the current process instance by instance id
- lookup of user tasks
- lookup of external tasks
- collection-returning queries for advanced custom-stage use

It does not own waiting or polling behavior.

### 7.2 Assert API

The assert API is responsible for:

- waiting/polling semantics
- presence/uniqueness enforcement
- failure messages
- process-state assertions

### 7.3 Restrictions

Restrictions are explicit in public query/assert methods from day one.

Restriction handling rules:

- restrictions are applied first
- optional predicate filtering happens afterward
- unsupported restrictions fail eagerly
- restriction support is modeled through `RestrictionAware` on the testing APIs

### 7.4 Predicates

Task predicates are optional and use `java.util.function.Predicate<TaskInformation>` directly in the public contract.

If no predicate is supplied, the effective predicate is match-all on the already restricted result set.

Process assertions remain parameter-based in v1. There is no `Predicate<ProcessInformation>` public model in v1.

## 8. Scenario State Model

The common stages carry:

- one grouped `ProcessTestContext`
- one current `instanceId`
- one current `ProcessInformation`
- one current `TaskInformation`
- one captured `Throwable`

Rules:

- `instanceId` is authoritative
- `ProcessInformation`, when present, must refer to the same instance id
- one scenario covers one process instance only
- one current task slot is used regardless of task type

## 9. Public JGiven Stage Model

### 9.1 Stage structure

The public JGiven model consists of:

- `ProcessStage`
- `AssertStage`
- `ActionStage`

`ActionStage` and `AssertStage` are sibling specializations of `ProcessStage`; neither inherits
the other's outcome/action verbs. `ProcessStage` owns shared scenario state and the operational
selection and synchronization steps that can prepare a later action or assertion.

### 9.2 Naming

The DSL uses:

- standard Java/Kotlin camelCase method names
- sentence-like JGiven descriptions supplied with `@Description` where a more natural report label is useful
- tense-based naming conventions

Guideline:

- `ActionStage` methods describe actions or events
- `AssertStage` methods describe asserted outcomes or observed states
- every public stage method returns its concrete stage via `self()` for Java/JGiven chaining;
  Kotlin `Step` blocks ignore that return value
- methods with Kotlin default arguments use `@JvmOverloads` so their ergonomic defaults are also
  available to Java customers
- customer stage implementations must be non-final because JGiven creates reporting subclasses

### 9.3 Reusable steps

A reusable scenario fragment is a receiver function: `Step<STAGE> = STAGE.() -> Unit`. Define
one with `step { ... }` and execute it with `given(...)`, `whenever(...)`, or `then(...)`:

```kotlin
val deploy: Step<MyActionStage> = step {
  processIsDeployed("process.bpmn")
}

given(deploy)
```

This keeps individual operations as standalone steps and makes their stage receiver explicit.

### 9.4 Basic verb set

The public common stages provide generic baseline verbs out of the box instead of exposing only low-level helpers.

The minimum v1 generic process testing surface includes:

- deployment
- start by definition and message
- task selection
- task completion/failure
- process wait assertions
- process path assertions
- process finished/incidents assertions
- message correlation
- signal delivery
- time passage
- opt-in exception capture and exception assertions

### 9.5 Access model

The stages expose:

- convenience methods first
- direct protected access to sub-interfaces second

This allows advanced customer stages to go lower-level without changing the public dependency boundary.

## 10. Task Selection Semantics

### 10.1 Selection style

Task selection supports:

- explicit selection verbs
- convenience combined verbs that select and act in one step

### 10.2 Selection strictness

Selection is strict:

- zero matches is a failure
- more than one match is a failure
- exactly one match becomes the current selected task

### 10.3 User and external task handling

The API distinguishes user and external task selection explicitly.

The process-waiting DSL distinguishes:

- `processWaitsInUserTask(...)`
- `processWaitsInElement(...)`

`processWaitsInUserTask(...)` and explicit selection verbs populate the same current `TaskInformation`.

An explicit `externalTaskIsSelected(...)` verb exists. A symmetric `processWaitsInExternalTask(...)` is not required in v1.

### 10.4 Exclusions

Direct task-id-driven public selection is not part of v1.

## 11. Process Assertions

The common API supports:

- process waiting in user task
- process waiting in element
- process has passed
- process has passed in order
- process has not passed
- process is finished
- process has incidents

Process wait/assert semantics must be canonicalized across engines, even if current adapter test stages differ today.

## 12. Payload Model

Payload-bearing public verbs use `Map<String, Any?>`.

V1 does not provide payload convenience overloads such as single key/value shortcuts. Empty payload is expressed with `Map.of()` or
equivalent.

## 13. Exception Model

Exception capture is opt-in only.

Rules:

- normal action verbs fail fast by default
- the stage provides a generic capture mechanism that can wrap arbitrary custom business logic
- assertion verbs can verify captured exception types afterward

## 14. Time Control

Time control is part of v1, but modeled as a test capability rather than a restriction.

Rules:

- capability support is explicit and separate from `RestrictionAware`
- unsupported time control fails fast with a clear message
- both C7 and C8 are expected to support time manipulation
- time control is not part of the first cross-engine acceptance scenario

### 14.1 Asynchronous Action Synchronization

Every process-affecting action can cause asynchronous continuation: starting a process, completing
a task, correlating a message or signal, advancing time, or updating process state. The operation
made available through `ProcessTestRuntimeApis` must complete only after its observable,
engine-specific side effects are available to the context query and assertion APIs. Completion must
not mean merely that the engine accepted the command.

Customer scenarios synchronize on a meaningful outcome using an assertion or selection step, for
example `processWaitsInUserTask("approveOrder")` or `processIsFinished()`. Assertion APIs own
polling, timeout, and diagnostics. There is no generic `processContinues()` customer step or
process-continuation capability: it has no portable completion criterion.

Adapters may deterministically release a concrete deferred unit of work in embedded test mode, or
await the engine's normal executor in live mode. Those mechanisms remain adapter internals and
must produce the same public completion semantics.

## 15. Bootstrap and Initializer Model

### 15.1 Discovery

Initializers are discovered via Java `ServiceLoader`.

### 15.2 Naming

Each initializer exposes a stable qualifier name aligned with existing adapter qualifier vocabulary, for example:

- `c7embedded`
- `c7remote`
- `c8`

### 15.3 Selection

V1 selection uses:

- annotation on test class hierarchy
- fallback to auto-selection if exactly one initializer is available

The annotation selects a qualifier and may provide adapter-specific `key=value` configuration.
Configuration is parsed into the initialization request, with malformed entries rejected before
the initializer is invoked. The shared testing API does not interpret adapter-specific keys.

The annotation may also name one or more `ProcessTestPropertyProvider` classes. A provider is a
framework-neutral SPI with a public no-argument constructor. It receives the concrete test class
and the explicit annotation configuration, then returns additional properties before the selected
initializer is invoked. This supports optional integrations that load framework configuration, for
example a Spring configuration-data provider using the explicitly declared `spring.profiles.active`
value. A provider must not require the application context it is helping configure to already exist.

Properties are merged deterministically:

- later declared providers override earlier providers;
- explicit annotation configuration overrides every provider value;
- the selected adapter may apply its own defaults only for values absent from the resolved map.

The resolved map is the configuration in `ProcessTestInitializationRequest`.

### 15.4 Resolution scope

Annotation resolution scans:

- concrete test class
- superclass chain

It does not scan interfaces or enclosing classes in v1.

### 15.5 Responsibility boundary

Bootstrap belongs outside scenario steps.

The initializer is responsible only for engine test context creation and cleanup. It is not responsible for customer domain/application
driver setup.

### 15.6 Initialization request

The initializer receives a compact request object, not the full test instance. The request includes
the concrete test class and configuration declared on `@UseProcessTestInitializer`.

### 15.7 Diagnostics

Resolution failures must include:

- requested qualifier, if any
- discovered qualifiers
- initializer implementation class names

## 16. Deployment Model

Deployment remains an explicit test action. It is not implicit initializer behavior.

The DSL supports:

- a convenience classpath-resource deployment verb
- a canonical deployment verb using `DeployBundleCommand`

This is required for scenarios that deploy mocks or substitute subprocess implementations explicitly.

## 17. Scope Exclusions

The following are out of scope for v1:

- decision-testing DSL
- customer-facing task subscriptions
- process stop/cancel DSL until corresponding runtime APIs exist
- raw engine-object escape hatches
- multi-instance generic scenario model

## 18. Acceptance Architecture

### 18.1 Shared fixture

The cross-engine proof uses a shared fixture module containing:

- the common customer test
- shared business stages
- shared constants
- shared logical resource names

### 18.2 Runner modules

Runner modules are engine-specific and thin. They provide:

- engine-specific dependencies
- engine/application setup
- engine-specific BPMN resources under shared logical names
- an annotation-only wrapper test class selecting the initializer

The wrapper class should contain no extra setup logic beyond annotation and inheritance if possible.

### 18.3 Shared scenario invariants

The following must remain identical across runners:

- shared test source
- shared business stage source
- shared constants

Only runner setup and resources may vary.

## 19. First Mandatory Acceptance Scenario

The first mandatory unchanged-test proof targets:

- `c7remote`
- `c8`

It must deliberately avoid embedded-only assumptions.

The first shared scenario covers:

1. explicit deployment
2. process start
3. external task selection and completion
4. user task selection and completion
5. message correlation after task completion
6. process path assertion
7. process finished assertion

Time control is intentionally excluded from the first proof slice.
