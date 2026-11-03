# `:backend` Module — Standalone Ktor & Exposed Server

## 1. Current Reality (Scope & Key Packages)
- **Type:** Pure Kotlin/JVM application (`com.lukasz.witkowski.training.planner.backend.ApplicationKt`, port `8080`).
- **Philosophy:** Intentionally small, monolithic, and boring—exists to provide realistic REST, pagination, versioning, conflict, auth, and WebSocket scenarios for the Android app.
- **Tech Stack:** Ktor `3.6.0` (Netty), JetBrains Exposed `1.5.0` (`org.jetbrains.exposed.v1.*`), HikariCP, H2 (in-memory dev/test) & PostgreSQL, BCrypt, JWT (`JwtConfig`).
- **Key Packages:**
  - `db/Tables.kt` & `DatabaseFactory.kt`: `UsersTable`, `CategoriesTable`, `ExercisesTable` (`revision`, `changeVersion`), `ExerciseDeletionsTable`, `IdempotencyLogsTable`, `ExerciseCategoriesTable`, `ExerciseImagesTable`, `ExerciseRecommendationsTable`, `TrainingPlansTable`, `TrainingExercisesTable`.
  - `routing/` & `service/`:
    - `AuthRoutes.kt` / `AuthService.kt`: Register, login, refresh, `/me`.
    - `ExerciseRoutes.kt` / `ExerciseService.kt`: Paginated list, CRUD, idempotency key deduplication (`IdempotencyLogsTable`), optimistic concurrency (`revision` $\to$ `409 Conflict`), and delta sync (`/api/v1/exercises/changes`).
    - `TrainingPlanRoutes.kt` / `TrainingPlanService.kt`: CRUD for training plans.
    - `ChatWsRoutes.kt`: WebSocket endpoint (`/ws/chat`) with JWT authentication (currently in-memory broadcast only).

## 2. Architecture & Coding Invariants
- **Shared Contracts:** Always use `@Serializable` DTOs and route constants from `:dto` (`ApiRoutes`). Never duplicate DTO classes inside `:backend`.
- **Exposed v1 Imports:** Use `org.jetbrains.exposed.v1.*` packages.
- **Device-Only State Exclusion:** Never store live workout timers, active set progress, or transient phone/watch execution state on the backend.
- **Testing:** Every new route or service behavior must be tested with Ktor `testApplication` (`ktor-server-test-host`) and H2 in `src/test/kotlin/.../backend/`.

## 3. Target Direction (`KEEP & EXPAND`)
- **KEEP:** Existing Auth, Exercise sync/idempotency/conflict engine, and Ktor/Exposed setup.
- **EXPAND:**
  1. **Global Chat Persistence & Pagination:** Add `ChatMessagesTable` and `GET /api/v1/chat/messages` paginated history endpoint alongside `/ws/chat`.
  2. **Completed Session History:** Add tables and endpoints for uploading and syncing completed `TrainingSession` records.
  3. **Exercise Version History:** Support storing/publishing community exercise versions so plans referencing older versions remain intact until upgraded.
  4. **SSE Experiment Endpoint:** Add `GET /api/v1/events` Server-Sent Events stream for the Android SSE learning experiment.

## 4. Verification Commands
```bash
./gradlew :backend:test :backend:ktlintCheck
```
