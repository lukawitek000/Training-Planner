# `:session` Module — Live Workout Execution State Machine & UI

## 1. Current Reality (Scope & Key Packages)
- **Namespace:** `com.lukasz.witkowski.training.planner.session`
- **Role:** Owns **`TrainingSession` (Execution)**—the active, device-local workout state machine and UI.
- **Domain (`domain/`):**
  - `session/TrainingSession.kt` & `session/TrainingSessionFactory.kt`: In-memory aggregate root controlling workout progression (`IdleState`, `ExerciseState`, `RestTimeState`, `SummaryState`), supporting `start`, `complete`, `skip`, `endEarly`, `addExtraSet`, `removeExtraSet`, `handle(SessionCommand)`, and `stop`.
  - `session/phases/`: `SessionPhase` (`IdlePhase`, `ExercisePhase`, `RestPhase`, `SummaryPhase`), `PhaseState`, and `NextPhaseProvider`. Phases manage their own `PhaseState` and `Timer`, expose `hasAutoFinished: Flow<Boolean>`, and act as pure generators of `SessionEvent?` (`start()`, `complete()`, `skip()`, `endEarly()`) without depending on `SessionStatisticsRecorder` or `TimeProvider`.
  - `session/queue/`: `ExerciseQueue` and `ExerciseQueueProgress` managing the remaining `SessionExercise` queue, dynamic set adjustments, and carrying over updated set configurations.
  - `session/TrainingSetsPolicy.kt`: `StraightSetsPolicy`.
  - `session/statisticsrecorder/`: `SessionStatisticsRecorder`, `SessionStatisticsProvider`, `CompositeStatisticsRecorder`, `BasicStatisticsRecorder`, `SessionEvent`, and `TimeProvider`.
  - `timer/`: `Timer`, `TimerFactory`, and `TimerState` (monotonic `elapsedTimeMillis()` countdown/overflow timer).
  - `models/`: `TrainingStatistics`, `ExerciseStatistics`, `ExerciseAttemptStatistics` (actual reps, actual weight in `Double?`, hold duration, rest duration, attempt time, RPE, notes).
  - `SessionCompletedListener.kt`: Interface implemented by `:analytics` (`AnalyticsSessionCompletedListener`) to persist completed `TrainingStatistics`.
- **Application (`application/`):** `TrainingSessionService` (coordinates `TrainingSession`, `TimerFactory`, and `SessionCompletedListener`).
- **Presentation (`presentation/`):** `TrainingSessionScreen`, `TrainingExerciseScreen`, `RestTimeScreen`, `TrainingSessionSummaryScreen`, `TrainingSessionViewModel`, and custom execution components (`AppStepper`, `AppRpeScaleGrid`, `AppFeedbackChips`).

## 2. Architecture & Coding Invariants
- **Device-Local Execution Invariant:** Active workout execution (`TrainingSession`, phases, timers, current set/exercise state) is strictly a device-local concern and must **never** depend on a backend network connection.
- **Phase Encapsulation Invariant:** `TrainingSession` must orchestrate phases polymorphically through the `SessionPhase` contract (`start()`, `complete()`, `skip()`, `endEarly()`, `handle(command)`, `hasAutoFinished`, `PhaseState.toTrainingSessionState`) without `is ExercisePhase` / `is RestPhase` / `is SummaryPhase` type checks.
- **Aggregate-Level Testing via `TrainingSessionFactory`:** Domain tests for workout progression and statistics must test `TrainingSession` as an aggregate initialized through `TrainingSessionFactory.create(...)` rather than testing internal phase or queue classes in isolation.
- **Deterministic Time in Tests:** Always inject `TimeProvider` (`FixedTimeProvider` in tests) and `TimerFactory` into `TrainingSessionFactory` and `TrainingSessionService`—never call `System.currentTimeMillis()` or `SystemClock.elapsedRealtime()` directly.

## 3. Target Direction & Known Tech Debt (`KEEP / REFACTOR`)
- **KEEP:** Core `TrainingSession` aggregate and `SessionPhase` state machine, `StraightSetsPolicy`, and Compose execution screens (`TrainingExerciseScreen`, `RestTimeScreen`, `TrainingSessionSummaryScreen`).
- **REFACTOR:**
  1. **`TrainingSessionService` & ViewModel Alignment:** Simplify `TrainingSessionService` so UI/ViewModels dispatch `SessionCommand`s and lifecycle actions cleanly without legacy wrapper methods.
  2. **Process-Death & Persistence:** Currently `TrainingSession` lives purely in memory inside `TrainingSessionService`, and only writes to Room (via `:analytics` `SessionCompletedListener`) when the session finishes. Align with the target lifecycle (`NOT_STARTED -> IN_PROGRESS -> PAUSED -> COMPLETED / CANCELLED`) so active sessions survive configuration changes/process death and integrate cleanly with `:session-service`.
  3. **Post-Completion Upload Flow:** After saving the completed session locally, prompt the user `"Upload training?"` (`Not now` vs `Upload`) or auto-upload if enabled in user preferences.

## 4. Verification Commands
```bash
./gradlew :session:testDebugUnitTest :session:ktlintCheck
```
