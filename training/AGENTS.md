# `:training` Module — Training Plan Management & Intent Domain

## 1. Current Reality (Scope & Key Packages)
- **Namespace:** `com.lukasz.witkowski.training.planner.training`
- **Role:** Owns **`TrainingPlan` (Intent)**—what the user intends to perform, distinct from actual workout execution (`:session`).
- **Domain (`domain/`):**
  - `TrainingPlan`, `TrainingPlanOverview`, `TrainingPlanConfiguration`, `TrainingExercise`, and `ExerciseSnapshot` (detaches the plan from live exercise mutations and stores `revision: Long? = 1L`).
  - `TrainingPlanRepository` and `TrainingPlanService`.
- **Infrastructure (`infrastructure/`):**
  - **Room 3 (`TrainingPlanDatabase`):** `TrainingPlanDao`, `DbTrainingPlanRepository`, `DbTrainingPlan`, `DbTrainingExercise`, `DbExerciseSnapshot`, `DbExerciseCategory`.
  - No remote/backend synchronization exists in `:training` yet (even though `:backend` already has `TrainingPlanRoutes`).
- **Presentation (`presentation/`):**
  - `TrainingsScreen` (`TrainingsListViewModel`, `TrainingsListUiState`).
  - `TrainingPlanDetailsScreen` (`TrainingPlanDetailsViewModel`).
  - `TrainingPlanEditorScreen` / `EditTrainingPlanScreen` (`TrainingPlanEditorViewModel`, MVI `TrainingConfigurationReducer` + `TrainingPlanEditingIntent`, drag-and-drop reorderable exercise list).
  - `DeleteTrainingPlanDialog` (`DeleteTrainingPlanViewModel`).
  - *Note:* `AddTrainingExerciseScreen` and `TrainingExerciseConfigurationScreen`/`ViewModel` currently live in `:app/src/main/kotlin/.../training/editor/`.

## 2. Architecture & Coding Invariants
- **Intent vs. Execution Invariant:** `TrainingPlan` and `TrainingExercise` must only store planned/target parameters (`repetitions`, `sets`, `restTime`, `weightInKg`, `time`). Never store completed session metrics here (except `lastSession: Instant?` metadata).
- **Exercise Version Pinning:** `ExerciseSnapshot` retains the `revision` of the exercise at the time the plan was configured. Cloud updates to community exercises must **never silently alter** `ExerciseSnapshot` inside an existing `TrainingPlan`.
- **Reducer Pattern:** State transitions in the plan editor go through pure `TrainingConfigurationReducer` functions (`TrainingConfigurationReducerTest`).

## 3. Target Direction & Known Tech Debt (`KEEP / REFACTOR / EXPAND`)
- **MOVE:** Bring `AddTrainingExerciseScreen` and `TrainingExerciseConfigurationScreen`/`ViewModel` from `:app` into `:training`.
- **EXPAND:**
  1. Add exercise update detection & explicit user inspection/upgrade flow when a newer `Exercise.metadata.revision` arrives from the backend.
  2. Support predefined/community plans that can be customized (cloning into a user-owned plan without destroying the predefined plan).
  3. Introduce `ScheduledTraining` (intention to perform a `TrainingPlan` at a date/time/location, one-off or recurring) or create a dedicated scheduling package.
  4. Add local-first backend synchronization for user/community training plans (following `:exercise`'s Outbox pattern).

## 4. Verification Commands
```bash
./gradlew :training:testDebugUnitTest :training:ktlintCheck
```
