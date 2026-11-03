---
name: local-first-sync
description: >-
  Use when implementing or modifying Room 3 persistence, offline mutation queues
  (Outbox pattern), WorkManager synchronization workers, idempotency keys,
  versioned community content, or explicit conflict resolution between Android
  modules and the Ktor backend.
---

# Local-First & Offline Synchronization Skill

Use this skill when building or debugging Room 3 persistence, offline-first repositories, WorkManager sync workers, or backend synchronization endpoints.

## Canonical Reference Implementation
The **`:exercise`** module is the golden reference for local-first sync in this repository. Inspect these files when designing sync for `:training`, `:session`, or `:analytics`:
- **Local Outbox & Conflict Resolution:** [`DefaultExerciseRepository.kt`](../../../exercise/src/main/kotlin/com/lukasz/witkowski/training/planner/exercise/infrastructure/DefaultExerciseRepository.kt)
- **Mutation & Delta Sync Engine:** [`DefaultExerciseSyncRepository.kt`](../../../exercise/src/main/kotlin/com/lukasz/witkowski/training/planner/exercise/infrastructure/sync/DefaultExerciseSyncRepository.kt)
- **WorkManager Scheduling:** [`WorkManagerExerciseMutationScheduler.kt`](../../../exercise/src/main/kotlin/com/lukasz/witkowski/training/planner/exercise/infrastructure/sync/WorkManagerExerciseMutationScheduler.kt), [`ExerciseMutationWorker.kt`](../../../exercise/src/main/kotlin/com/lukasz/witkowski/training/planner/exercise/infrastructure/sync/ExerciseMutationWorker.kt)
- **Backend Idempotency & 409 Conflict Handling:** [`ExerciseRoutes.kt`](../../../backend/src/main/kotlin/com/lukasz/witkowski/training/planner/backend/routing/ExerciseRoutes.kt), [`ExerciseService.kt`](../../../backend/src/main/kotlin/com/lukasz/witkowski/training/planner/backend/service/ExerciseService.kt)

## Mandatory Local-First Checklist

### 1. Room 3 (`androidx.room3.*`) as Authoritative SSOT
- Always import `androidx.room3.*` (never `androidx.room.*`).
- UI and ViewModels must observe local Room `Flow`s or `PagingData`—never fetch directly from Retrofit into UI state for persistent domain objects.
- Persist local writes and outbox mutations (`STATE_PENDING`) before enqueueing background sync.

### 2. Outbox Queue & Idempotency Keys
- Every client-originated `CREATE`, `UPDATE`, or `DELETE` mutation must generate a `UUID.randomUUID().toString()` `idempotencyKey` and record the `expectedVersion` (`revision`).
- If an entity with a pending `TYPE_CREATE` mutation is updated locally before sync runs, coalesce the update into the existing `TYPE_CREATE` mutation (preserving its `idempotencyKey`).
- If an unsynced local-only entity is deleted before ever reaching the server, delete both the entity and its pending mutation locally without queueing a remote `DELETE`.

### 3. WorkManager & User Sync Preferences
- Check `AuthSessionProvider` (user is authenticated) and `SyncPreferencesStorage` (`isBackgroundSyncEnabled` or session auto-upload preference) before scheduling background work.
- Use `ExistingWorkPolicy.KEEP` or `APPEND_OR_REPLACE` appropriately with `NetworkType.CONNECTED` constraints.

### 4. Explicit User Conflict Resolution (HTTP 409)
- When the server returns `409 Conflict` (version mismatch), **never silently overwrite** local changes.
- Mark the local entity as `CONFLICT`, store the server payload snapshot (`conflictServerJson`), and surface an explicit user decision in UI:
  - **Keep Local (`resolveConflictKeepLocal`):** Update `expectedVersion` to `latestServerVersion`, generate a fresh `idempotencyKey`, set state back to `PENDING_UPLOAD`, and re-enqueue the mutation worker.
  - **Accept Server (`resolveConflictAcceptServer`):** Replace local entity with the server version (preserving local-only image references if applicable) and delete pending mutations for that entity.

## Verification
1. Write unit tests for repository/sync logic using `StandardTestDispatcher`, `Turbine`, and `MockWebServer` (or Robolectric for Room/WorkManager).
2. Run `./gradlew :<module>:testDebugUnitTest :backend:test`.
