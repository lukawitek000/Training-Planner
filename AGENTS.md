# AGENTS.md — Training Planner (Android & Kotlin Engineering Lab)

Local-first workout planning and execution application serving as the primary engineering laboratory for Senior Android, Kotlin, offline synchronization, and background execution mastery. Canonical product spec: [`docs/product_domain_direction.md`](docs/product_domain_direction.md).

---

## 1. Socratic & Deliberate Learning Protocol (MANDATORY)

The resulting application is a **side effect of the learning process**:
$$\text{Learn} \longrightarrow \text{Design} \longrightarrow \text{Implement} \longrightarrow \text{Break} \longrightarrow \text{Test} \longrightarrow \text{Debug} \longrightarrow \text{Explain} \longrightarrow \text{Interview}$$

When designing, implementing, or refactoring non-trivial code, you **MUST**:
1. **Surface Trade-Offs & Failure Modes:** Briefly explain why an approach is chosen over alternatives, and highlight lifecycle, coroutine cancellation, or offline concurrency edge cases.
2. **Offer a "Break / Debug / Interview Checkpoint":** After completing a feature or refactoring step, provide 1–2 probing senior-level interview questions or a concrete failure scenario to test.
3. **Audit Before Rewriting:** Treat the codebase as a deliberate migration (`KEEP / REFACTOR / REWRITE / MOVE / REMOVE / DEFER`). Activate the `architecture-audit` skill before restructuring modules or domain boundaries.

---

## 2. Core Domain Invariants

1. **Intent (`TrainingPlan`) vs. Execution (`TrainingSession`):**
   - `TrainingPlan` holds **intended targets** (`sets`, `repetitions`, `weightInKg`, `restTime`, `time`).
   - `TrainingSession` (and `TrainingStatistics`) holds **actual performed execution** (completed sets/reps/weights, timestamps, RPE, deviations).
   - `ScheduledTraining` represents the intention to perform a plan at a specific time, never a completed session.
2. **Exercise Versioning & Immutability in Plans:**
   - Community/backend exercises are versioned (`revision: Long`).
   - A `TrainingPlan` stores an `ExerciseSnapshot` pinned to the exact `revision` it was configured against.
   - Cloud updates **never silently mutate** existing plans; the user must inspect and explicitly accept updates.
3. **Local-First Single Source of Truth (SSOT):**
   - Local Room (`androidx.room3`) is the authoritative SSOT. The app must work offline.
   - Completed training sessions save locally first; uploading is optional (user prompt or auto-upload preference).
   - Favor **explicit user conflict resolution** ("Keep Local" vs. "Accept Server", as in `:exercise`) over silent auto-merging.

---

## 3. Accurate 14-Module Architecture Map (`settings.gradle.kts`)

> **Architecture Reality Note:** Client modules currently use Android Library (`com.android.library` via `buildSrc`, source in `src/main/kotlin`) and share pure JVM `:dto` with `:backend`. Do **not** generate KMP `commonMain`/`androidMain` source sets until a KMP migration branch is explicitly started.

- **[`:app`](app/AGENTS.md)** — Android application entry point, Koin `appModule`, and Jetpack Navigation 3 root graph.
- **[`:shared`](shared/AGENTS.md)** — Design system (`App*` composables), `Dimens`, `TrainingPlannerTheme`, `@AppPreview`, `AppResult`, `TimeProvider`, and coroutine dispatchers.
- **[`:dto`](dto/AGENTS.md)** — Pure Kotlin/JVM `@Serializable` network contracts and `ApiRoutes` shared between Android and `:backend`.
- **[`:exercise`](exercise/AGENTS.md)** — **Reference Module:** Exercise catalog (`Exercise2`), Room 3 + Paging 3, WorkManager outbox sync (`DbExerciseMutation`), and conflict resolution.
- **[`:training`](training/AGENTS.md)** — `TrainingPlan` (Intent), `TrainingExercise`, `ExerciseSnapshot`, `TrainingPlanDatabase`, and MVI editor reducer.
- **[`:session`](session/AGENTS.md)** — Live workout execution state machine (`TrainingSession`, `StraightSetsPolicy`, `CircuitSetsPolicy`, `Timer`) and execution Compose UI.
- **[`:session-service`](session-service/AGENTS.md)** — Android Foreground Service (`SessionService`) and ongoing workout notifications.
- **[`:analytics`](analytics/AGENTS.md)** — Completed workout persistence (`StatisticsDatabase`, `TrainingStatistics`) via `SessionCompletedListener`.
- **[`:synchronization`](synchronization/AGENTS.md)** — *Legacy:* Currently holds Wear OS Bluetooth `ChannelClient` streaming (not backend HTTP sync).
- **[`:image`](image/AGENTS.md)** — Local file storage + Room 3 reference-counted `ImageReferenceDatabase` + Adler32 checksum deduplication.
- **[`:network`](network/AGENTS.md)** — Retrofit 3 + OkHttp 5 infrastructure, `AuthInterceptor`, `TokenAuthenticator` (Mutex 401 refresh), `TokenStorage`, and `SyncScheduler`.
- **[`:auth`](auth/AGENTS.md)** — Login/register/refresh flows, Android Keystore AES-GCM encryption (`AndroidSecureTokenStorage`), and auth UI.
- **[`:user`](user/AGENTS.md)** — User profile, DataStore `AndroidUserPreferencesStorage` (implements `SyncPreferencesStorage`), and profile UI.
- **[`:backend`](backend/AGENTS.md)** — Standalone JVM server: Ktor 3.6 + JetBrains Exposed 1.5 + H2/PostgreSQL + JWT + WebSockets (`/ws/chat`).

---

## 4. Global Tech Stack & Coding Standards

- **Toolchain:** Kotlin `2.4.10`, Java 21, AGP `9.3.3`, `compileSdk = 37`, `minSdk = 28`.
- **Room 3:** Always import `androidx.room3.*` (never `androidx.room.*`).
- **Networking & Serialization:** Retrofit 3 + OkHttp 5 + `kotlinx.serialization` (never introduce `Gson` in new code).
- **Dependency Injection:** Koin `4.2.2` (`io.insert-koin.compiler.plugin`).
- **Error Handling:** Use `AppResult<T, E>` and `ResultHandler` from `:shared` for expected domain/IO failures.
- **Compose UI (`docs/composable_guidelines.md`):**
  - Strictly Unidirectional Data Flow (immutable `UiState`, sealed intents/events).
  - Prefix custom design system components with `App` (e.g., `AppButton`).
  - **Zero raw `.dp` or `.sp` literals** in UI files; always use `Dimens.*` from `:shared`.
  - Annotate previews with `private` `@AppPreview` wrapped in `TrainingPlannerTheme`.
- **Git Branch Rule:** Always work on dedicated feature/study branches (`study/<topic>` or `refactor/<module>`). Never commit directly to `master` or `develop`.

---

## 5. Model Selection, Subagents & CLI Workflows

### Targeted Gradle Verification (Token & Time Saver)
Always scope Gradle tasks to the modified module(s) rather than running full-repo builds:
```bash
./gradlew :<module>:testDebugUnitTest :<module>:ktlintCheck
./gradlew :backend:test :dto:test
```

### Model & Subagent Routing
- **Gemini Pro / High Reasoning (`pro`):** Use for multi-module architecture audits, Room/WorkManager sync & conflict flows, concurrency/Mutex/lifecycle debugging, and Socratic interview grilling.
- **Gemini Flash (`flash`):** Use for standard feature implementation, Compose UI + `@AppPreview` rendering, ViewModel/DAO unit tests (`MockK`, `Turbine`), and Ktor route tests.
- **Gemini Flash-Lite (`flash_lite`):** Use for `ktlintCheck`/`detekt` fixes, DTO boilerplate, string resource extraction, and quick symbol lookups.
- **Subagent Threshold:** Spawn a subagent (`research`, `self`, or custom `module-auditor` / `sync-and-concurrency-breaker` / `gradle-verifier`) whenever a task requires reading **> 4 files across 2+ modules**, running noisy multi-module Gradle test suites, or performing an adversarial code review.
