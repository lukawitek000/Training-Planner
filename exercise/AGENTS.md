# `:exercise` Module — Exercise Catalog & Reference Offline-Sync Architecture

## 1. Current Reality (Scope & Key Packages)
- **Namespace:** `com.lukasz.witkowski.training.planner.exercise`
- **Role:** Canonical **reference implementation** in this codebase for local-first persistence, Paging 3, offline mutation queueing (Outbox pattern), WorkManager background sync, and explicit user conflict resolution.
- **Domain (`domain/`):**
  - `Exercise` (canonical model with `ExerciseMetadata`, `ExerciseSource`, `ExerciseSharing`, `revision: Long`, `ExerciseSyncState`) and `ExerciseDetails` (includes beginner/intermediate/advanced `ExerciseRecommendation`).
- **Infrastructure (`infrastructure/`):**
  - **Room 3 (`ExerciseDatabase`):** Pre-populated from `assets/database/exercises.db` with bundled WebP images in `assets/exercise_images/`. Tables: `DbExercise`, `DbExerciseCategory`, `ExerciseCategoryCrossRef`, `DbExerciseRecommendation`, `DbExerciseImage`, `DbExerciseMutation`, `DbSyncMetadata`.
  - **Local-First & Paging:** `DefaultExerciseRepository` queries Room via Paging 3 (`Pager` + `RoomExerciseLocalDataSource`) and writes mutations (`DbExerciseMutation`) on insert/update/delete before triggering `ExerciseMutationScheduler`.
  - **Background Sync (`infrastructure/sync/`):** `DefaultExerciseSyncRepository`, `WorkManagerExerciseMutationScheduler` (implements both `ExerciseMutationScheduler` and `:network`'s `SyncScheduler`), `ExerciseMutationWorker` (pushes outbox with `idempotencyKey`), and `ExerciseRemoteChangesSyncWorker` (pulls delta changes via `/api/v1/exercises/changes`).
- **Presentation (`presentation/`):** `ExercisesScreen` (`ExercisesListViewModel`), `ExerciseDetailsScreen` (`ExerciseDetailsViewModel`, `ExerciseConflictDialog`), `CreateExerciseScreen` / `EditExerciseScreen` (`ExerciseEditorViewModel`), `DeleteExerciseDialog`.

## 2. Architecture & Coding Invariants
- Activate the **`local-first-sync`** skill whenever touching `DefaultExerciseRepository`, `DefaultExerciseSyncRepository`, or WorkManager workers.
- **Room 3:** Always use `androidx.room3.*` imports.
- **Conflict Resolution Invariant:** Never silently overwrite local user edits on HTTP `409 Conflict`. Persist conflict state (`SYNC_STATE_CONFLICT` + `conflictServerJson`) and let the user choose via `resolveConflictKeepLocal` or `resolveConflictAcceptServer` (`ExerciseConflictDialog`).

## 3. Target Direction & Known Tech Debt (`KEEP`)
- **KEEP:** Use this module's Outbox + Idempotency + Conflict Resolution pattern as the blueprint for `:training` and `:session` sync.

## 4. Verification Commands
```bash
./gradlew :exercise:testDebugUnitTest :exercise:ktlintCheck
```
