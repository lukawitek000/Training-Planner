---
name: architecture-audit
description: >-
  Use when auditing an existing Training Planner module, planning a migration or
  refactoring step, or deciding whether to KEEP, REFACTOR, REWRITE, MOVE, REMOVE,
  or DEFER existing components against the Product & Domain Direction.
---

# Architecture & Module Audit Skill

Use this skill whenever the user asks to audit a module, evaluate existing code against the product direction, or design a refactoring/migration roadmap.

## Step 1: Load the Product & Domain Direction
1. Read [`docs/product_domain_direction.md`](../../../docs/product_domain_direction.md) and the target module's `AGENTS.md`.
2. If auditing multiple modules (> 4 files across 2+ modules), delegate the file inspection to a `module-auditor` or `research` subagent so raw file reads do not bloat the main conversation context.

## Step 2: Evaluate Against the 5 Core Architectural Criteria
For every package, class, or database table in the scope, check:
1. **Intent vs. Execution Separation:** Does `TrainingPlan` hold only planned targets while `TrainingSession` / `TrainingStatistics` holds actual performed values?
2. **Exercise Version Pinning:** Are exercise versions (`revision: Long`) preserved on plans (`ExerciseSnapshot`) so cloud updates never silently mutate existing plans?
3. **Local-First SSOT (`androidx.room3`):** Is Room the primary source of truth with full offline usability, and are network calls decoupled via repositories/workers?
4. **Error & Concurrency Safety:** Are expected failures modeled with `AppResult<T, E>` (rather than uncaught exceptions), and are coroutines/dispatchers (`CoroutineDispatcherQualifiers`) injectable for deterministic unit testing?
5. **UI & Navigation Standards:** Do composables follow [`docs/composable_guidelines.md`](../../../docs/composable_guidelines.md) (`App*` components, `Dimens.*` tokens, `@AppPreview`) and Jetpack Navigation 3 patterns?

## Step 3: Produce the Standardized Audit Classification Table
Always present findings using this exact decision taxonomy:
- **`KEEP`**: Matches target domain and engineering standards; only minor polish needed.
- **`REFACTOR`**: Sound domain concept or structure, but implementation needs adjustment (e.g., wiring Koin, fixing lifecycle bugs, aligning Room schema).
- **`REWRITE`**: Concept is needed, but current implementation is incompatible with target direction (e.g., replacing legacy `Gson` Bluetooth streaming).
- **`MOVE`**: Valid code placed in the wrong module (e.g., `AddTrainingExerciseScreen` inside `:app` instead of `:training`).
- **`REMOVE`**: Dead code, duplicate legacy model (`Exercise` vs `Exercise2`), or unused Gradle dependency.
- **`DEFER`**: Out of scope for the current learning milestone; leave untouched with a clear note.

| Component / Class | Decision (`KEEP`/`REFACTOR`/`REWRITE`/`MOVE`/`REMOVE`/`DEFER`) | Current State & Gap | Actionable Next Step |
| :--- | :--- | :--- | :--- |
| `[ClassName](file:///...)` | ... | ... | ... |

## Step 4: Socratic Architecture Defense Checkpoint
Conclude every audit with **2 architectural trade-off questions** asking the user to confirm or defend the target boundary before modifying production code.
