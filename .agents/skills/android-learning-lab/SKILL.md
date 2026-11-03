---
name: android-learning-lab
description: >-
  Use when starting a new learning topic, implementing an Android platform
  experiment (ForegroundService, BroadcastReceiver, ContentProvider, SSE,
  WebSockets, Wear OS Data Layer), or running the Socratic Learn -> Design ->
  Implement -> Break -> Test -> Debug -> Explain -> Interview mastery loop.
---

# Senior Android Engineering Lab & Interview Mastery Skill

The Training Planner is a deliberate engineering vehicle for mastering senior-level Android, Kotlin, concurrency, and distributed local-first patterns. Activate this skill when exploring a new Android technology, running a platform experiment, or conducting a deep-dive review.

## The 8-Stage Mastery Loop

### Stage 1 & 2: Learn & Design (Before Writing Code)
1. Use `android docs search "<topic>"` (from the `android-cli` skill) or `retrieving-developer-knowledge` to fetch authoritative Android documentation if needed.
2. Present **2–3 architectural alternatives** (e.g., Bound vs. Started `ForegroundService`, `WorkManager` vs. `AlarmManager`, OkHttp WebSocket vs. SSE vs. FCM, `StateFlow` `SharingStarted.WhileSubscribed(5000)` vs. `Lazily`).
3. Ask the user to choose and briefly defend the design for the Training Planner use case.

### Stage 3: Implement
- Write clean, modular code adhering to module `AGENTS.md` invariants, injectable dispatchers (`CoroutineDispatcherQualifiers`), and `TimeProvider`.

### Stage 4 & 5: Break & Test (Adversarial Verification)
Propose and test at least **two realistic failure scenarios** for the mechanism just built:
- **Lifecycle & Process Death:** What happens when the user backgrounds the app, rotates the screen, or Android kills the process mid-workout (`SavedStateHandle`, Room recovery, `START_STICKY`)?
- **Structured Concurrency & Cancellation:** What happens if the collecting scope cancels mid-operation? Are `CancellationException`s rethrown properly inside `runCatching` / `try-catch` blocks?
- **Concurrency & Race Conditions:** What happens when two requests hit `401 Unauthorized` simultaneously (`Mutex` in `TokenAuthenticator`), or the user edits an exercise while `ExerciseMutationWorker` is in flight?
- **Offline & Network Flakiness:** Verify behavior with `MockWebServer` (HTTP 409, 500, socket timeout) and `Turbine` for `Flow` emissions.

### Stage 6, 7 & 8: Debug, Explain & Mock Interview
When the user asks to be grilled or finishes a milestone, conduct an interactive **3-Question Senior Android Mock Interview**:
1. **Mechanism Under the Hood:** Ask how the Android OS, Binder, Looper, SQLite WAL, or Kotlin Coroutine state machine executes this mechanism internally.
2. **Failure & Edge-Case Diagnosis:** Present a subtle production bug scenario related to the feature and ask the user how they would diagnose and fix it.
3. **Architectural Trade-Off Defense:** Ask "Why didn't you use X instead?" and evaluate the user's answer with constructive senior-staff feedback.

## Custom Subagent Definitions Reference
When a task benefits from isolated subagents, you can define and invoke these specialized personas using `define_subagent` + `invoke_subagent`:

1. **`module-auditor`** (`EnableWriteTools: false`, `Model: "pro"`):
   - *Role:* Audits a module against `docs/product_domain_direction.md` and returns a `KEEP / REFACTOR / REWRITE / MOVE / REMOVE / DEFER` table with clickable file links.
2. **`sync-and-concurrency-breaker`** (`EnableWriteTools: false`, `Model: "pro"`):
   - *Role:* Adversarial Staff Android Reviewer that hunts for coroutine cancellation bugs, Room transaction gaps, race conditions, and lifecycle leaks in modified files, returning concrete failure scenarios and interview questions.
3. **`gradle-verifier`** (`EnableWriteTools: true`, `Model: "flash"`):
   - *Role:* Runs `:module:testDebugUnitTest` and `:module:ktlintCheck`, fixes mechanical formatting/import issues, and returns a concise pass/fail summary.
