# `:analytics` Module — Historical Workout Persistence & Statistics

## 1. Current Reality (Scope & Key Files)
- **Namespace:** `com.lukasz.witkowski.training.planner.analytics`
- **Role:** Persists completed workout statistics emitted by `:session` into a local Room 3 database.
- **Domain & Application (`domain/`, `application/`):**
  - `StatisticsRepository`: `save(TrainingStatistics)`, `delete`, `getByTrainingPlanId`, `getByTrainingStatisticsId`.
  - `TrainingStatisticsService`: Application wrapper around `StatisticsRepository`.
- **Infrastructure (`infrastructure/`):**
  - `AnalyticsSessionCompletedListener`: Implements `:session`'s `SessionCompletedListener` to save completed `TrainingStatistics` into Room.
  - **Room 3 (`StatisticsDatabase`):** `StatisticsDao`, `DbStatisticsRepository`, `DbTrainingStatistics`, `DbExerciseStatistics`, `DbExerciseAttemptStatistics`, and relational models (`DbTrainingWithExercisesStatistics`, `DbExerciseWithAttemptsStatistics`).
- **Presentation:** None yet (no Compose screens or ViewModels exist in `:analytics`).

## 2. Architecture & Coding Invariants
- **Room 3:** Always use `androidx.room3.*` imports.
- Note that domain models `TrainingStatistics`, `ExerciseStatistics`, and `ExerciseAttemptStatistics` are defined in `:session` (`com.lukasz.witkowski.training.planner.session.domain.models`), while `:analytics` owns their Room persistence.

## 3. Target Direction & Known Tech Debt (`REFACTOR & EXPAND`)
- **Audit Boundary with `:session`:** During the architecture audit, decide whether `StatisticsDatabase` should merge with `TrainingSession` persistence (so `TrainingSession` represents both in-progress and completed history, comparing **Planned vs. Actual**) or remain a separate read/analytics store.
- **Fix Coroutine Contract:** `StatisticsRepository.delete(trainingStatistics)` is currently non-`suspend`; align with `suspend` DAO operations.
- **EXPAND:** Build history & statistics review screens (volume progression, planned vs. actual comparisons, RPE trends) and optional completed session upload to `:backend`.

## 4. Verification Commands
```bash
./gradlew :analytics:testDebugUnitTest :analytics:ktlintCheck
```
