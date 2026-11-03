# `:session` Module — Live Workout Execution State Machine & UI

## 1. Current Reality (Scope & Key Packages)
- **Namespace:** `com.lukasz.witkowski.training.planner.session`
- **Role:** Owns **`TrainingSession` (Execution)**—the active, device-local workout state machine and UI.
- **Domain (`domain/`):**
  - `session/TrainingSession.kt`: In-memory aggregate controlling workout progression (`IdleState`, `ExerciseState`, `RestTimeState`, `SummaryState`), supporting `start`, `completed`, `skip`, `skipExercise`, `addExtraSet`, `updateLastAttemptRpe`, `finalizeStatistics`, and `stop`.
  - `session/TrainingSetsPolicy.kt`: `StraightSetsPolicy` and `CircuitSetsPolicy`.
  - `session/statisticsrecorder/`: `SessionStatisticsRecorder`, `CompositeStatisticsRecorder`, `BasicStatisticsRecorder`, `SessionEvent`, `SessionStatisticsBuilder`.
  - `timer/Timer.kt`: Coroutine-based workout/rest timer.
  - `models/`: `TrainingStatistics`, `ExerciseStatistics`, `ExerciseAttemptStatistics` (actual reps, actual weight, duration, RPE, notes).
  - `SessionCompletedListener.kt`: Interface implemented by `:analytics` (`AnalyticsSessionCompletedListener`) to persist completed `TrainingStatistics`.
- **Application (`application/`):** `TrainingSessionService` (coordinates `TrainingSession`, `Timer`, and `SessionCompletedListener`).
- **Presentation (`presentation/`):** `TrainingSessionScreen`, `TrainingExerciseScreen`, `RestTimeScreen`, `TrainingSessionSummaryScreen`, `TrainingSessionViewModel`, and custom execution components (`AppStepper`, `AppRpeScaleGrid`, `AppFeedbackChips`).

## 2. Architecture & Coding Invariants
- **Device-Local Execution Invariant:** Active workout execution (`TrainingSession`, timers, current set/exercise state) is strictly a device-local concern and must **never** depend on a backend network connection.
- **Deterministic Time in Tests:** Always inject `TimeProvider` (`FixedTimeProvider` in tests) into `TrainingSession` and `TrainingSessionService`—never call `System.currentTimeMillis()` directly.

## 3. Target Direction & Known Tech Debt (`KEEP / REFACTOR`)
- **KEEP:** Core `TrainingSession` state machine, policies (`StraightSetsPolicy`, `CircuitSetsPolicy`), and Compose execution screens (`TrainingExerciseScreen`, `RestTimeScreen`, `TrainingSessionSummaryScreen`).
- **REFACTOR:**
  1. **Process-Death & Persistence:** Currently `TrainingSession` lives purely in memory inside `TrainingSessionService`, and only writes to Room (via `:analytics` `SessionCompletedListener`) when the session finishes. Align with the target lifecycle (`NOT_STARTED -> IN_PROGRESS -> PAUSED -> COMPLETED / CANCELLED`) so active sessions survive configuration changes/process death and integrate cleanly with `:session-service`.
  2. **Post-Completion Upload Flow:** After saving the completed session locally, prompt the user `"Upload training?"` (`Not now` vs `Upload`) or auto-upload if enabled in user preferences.

## 4. Verification Commands
```bash
./gradlew :session:testDebugUnitTest :session:ktlintCheck
```
