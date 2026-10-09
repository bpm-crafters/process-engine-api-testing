# AGENTS.md

## 1. Overview

`process-engine-api-testing` is a dedicated library family for customer-facing process testing on top of `process-engine-api`. Its purpose is to provide an engine-agnostic test contract, JGiven integration, and adapter pluggability for running the same customer scenario across different engine stacks.

## 2. Folder Structure

- `core`: engine-agnostic testing SPI and common contracts.
  - initializer SPI
  - grouped test context contracts
  - query API
  - assert API
  - capabilities and diagnostics contracts
- `jgiven`: JGiven/JUnit integration layer built on top of `core`.
  - base `AssertStage`
  - base `ActionStage`
  - test bootstrap integration
- `docs`: authoritative project documentation.
  - `constitution.md`: non-negotiable design principles and boundaries
  - `spec.md`: detailed system design and functional specification
  - `implementation-plan.md`: phased delivery plan
  - `adr/`: irreversible or high-cost architectural decisions

## 3. Core Behaviors & Patterns

- `core` defines only engine-agnostic contracts. It must not leak engine runtime classes.
- Adapter-specific implementations belong in adapter repositories, not here.
- The grouped test context is the single public root object; sub-interfaces partition responsibilities beneath it.
- Query and assertion responsibilities are separated:
  - query APIs return snapshot information only
  - assert APIs own waiting, polling, and assertion semantics
- Public JGiven stages are intended for customer process tests, not adapter-internal verification.

## 4. Conventions

- Treat `docs/spec.md` as the primary detailed description of intended behavior.
- Use ADRs only for decisions that are expensive to reverse or that define stable architecture boundaries.
- Keep naming consistent with `process-engine-api` vocabulary wherever possible.
- Prefer existing `process-engine-api` model types such as `TaskInformation` and `ProcessInformation` over introducing parallel abstractions.
- Public DSL naming is sentence-like and underscore-based when describing JGiven stage methods.

## 5. Working Agreements

- Do not treat exploratory code as authoritative over the docs. When design and code diverge, align code to the docs or update the docs deliberately.
- Preserve the engine-agnostic dependency boundary. Public contracts here must not depend on Camunda 7 or Camunda 8 classes.
- Keep customer-facing testing concerns separate from adapter-internal testing concerns.
- When changing the architecture, update `constitution.md`, `spec.md`, and any affected ADR in the same change.
- Record follow-on cross-repo work explicitly when changes here require adapter-side implementation.
