# AGENTS.md — Training Planner (Primary Android & KMP Curriculum Vehicle)

Local-first workout planning and execution application used as the primary hands-on vehicle for Senior Android & KMP engineering mastery (see [`product_domain_direction.md`](file:///Users/lukaszwitkowski/interviews/cv/interview_prep/training_planner_app/product_domain_direction.md)).

---

## 1. Role & Product Goal

The **Training Planner** is a realistic **local-first workout planning and execution platform** serving as the canonical engineering laboratory for mastering senior-level Android, Kotlin Multiplatform (KMP), modern background execution, and offline synchronization patterns.

The resulting application is a deliberate vehicle for learning:
$$\text{Learn} \longrightarrow \text{Design} \longrightarrow \text{Implement} \longrightarrow \text{Break} \longrightarrow \text{Test} \longrightarrow \text{Debug} \longrightarrow \text{Explain} \longrightarrow \text{Interview}$$

---

## 2. Core Domain Invariants & Architecture

### Intent vs. Execution SSOT
- **`TrainingPlan` (Intent):** Represents what the user intends to do. Contains exercise definitions and configured targets (sets, reps, rest time, target weight). Exercises can be bundled, user-created, or community-provided (with versions).
- **`TrainingSession` (Execution):** Represents what actually happens on the device during live workout execution (actual completed reps, weights lifted, timestamps, heart rate, deviations).
- **Room as Local SSOT:** The local Room SQLite database is the authoritative Single Source of Truth on the device. Remote synchronization is an enhancement, not a prerequisite for full offline operation.

### Modularization Map
- **`:app`** — Android application entry point, root navigation, and dependency injection graph.
- **`:training`** — Training plan management, creation, customization, and scheduling domain logic.
- **`:exercise`** — Exercise catalog, definitions, search, and versioned community content.
- **`:synchronization`** — Conflict-free background sync engine, queueing, and retry policies.
- **`:session-service`** — Foreground Service and notification management for uninterrupted workout execution.
- **`:statistics`** — Historical workout aggregation, volume calculations, and analytics.
- **`:wearable`** — Wear OS execution-only companion app module.
- **`:network`** — HTTP client, REST/Ktor communication, serialization, and auth interceptors.
- **`:auth`** — Authentication token lifecycle, session persistence, and security.
- **`:shared`** — Design system primitives, `@AppPreview`, `Dimens2`, and core shared utilities.
- **`:backend`** — Standalone Kotlin backend powered by Ktor and PostgreSQL.

---

## 3. Composable & Coding Standards

1. **Composable Guidelines (`docs/composable_guidelines.md`):**
   - Follow strict Unidirectional Data Flow (UDF) with immutable view states and sealed event classes.
   - Prefix custom design system components with `App` (e.g. `AppTextField`, `AppButton`).
   - Source all dimensions and spacing from `Dimens2` (no raw `.dp` or `.sp` literals).
   - Annotate previews with `@AppPreview` and wrap in `TrainingPlannerTheme2`.
2. **Dependency Injection:** Koin modules configured per feature.
3. **KMP Separation:** Keep pure domain models, business logic, and state machines in `commonMain`; isolate Android-specific components (`Room`, `ForegroundService`, `WorkManager`) to `androidMain`.

---

## 4. Key Workflows & CLI Commands

- **Unit Tests:**
  ```bash
  ./gradlew testDebugUnitTest
  ```
- **Module Verification:**
  ```bash
  ./gradlew :training:testDebugUnitTest :exercise:testDebugUnitTest :synchronization:testDebugUnitTest
  ```
- **Compose Previews:**
  Use Antigravity `compose-preview` sidecar (`android preview render`) to visually test and verify UI states.
- **Device & Emulator Testing:**
  Use `android-device-hub` sidecar (`android layout`, `android screen`) to inspect live session execution and service behavior.
- **Git Branch Rule:**
  Always work on dedicated feature/study branches (`study/<topic>` or `refactor/<module>`). Never commit architectural experiments directly to `master`.
