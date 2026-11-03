# `:dto` Module — Shared Client-Server Serialization Contracts

## 1. Current Reality (Scope & Key Files)
- **Type:** Pure Kotlin/JVM module (`kotlin("jvm")` + `kotlin("plugin.serialization")`). Zero Android dependencies.
- **Consumed By:** `:backend` (Ktor server) and Android modules (`:network`, `:auth`, `:user`, `:exercise`, `:shared`).
- **Key Packages (`com.lukasz.witkowski.training.planner.dto`):**
  - `common/ApiRoutes.kt`: Single source of truth for REST paths (`/api/v1/auth/*`, `/api/v1/exercises/*`, `/api/v1/training-plans/*`) and WebSocket path (`/ws/chat`).
  - `common/CommonDtos.kt`: Shared error responses (`ErrorResponseDto`), pagination wrappers, etc.
  - `auth/AuthDtos.kt`: Login, register, token refresh, and user profile DTOs.
  - `exercise/ExerciseDtos.kt`: Exercise CRUD, recommendations, sync delta changes, and conflict DTOs.
  - `training/TrainingPlanDtos.kt`: Training plan request/response DTOs.

## 2. Architecture & Coding Invariants
- **Pure JVM Constraint:** Never add Android (`androidx.*`, `android.*`), Room, Retrofit, or Ktor dependencies to `:dto`. Only `kotlinx.serialization` and standard Kotlin types are permitted.
- **Backwards/Forwards Compatibility:** Provide sensible default values on `@Serializable` properties where appropriate, and test round-trip JSON serialization in `DtoSerializationTest.kt`.
- Always update `ApiRoutes.kt` first when introducing new endpoints in `:backend` or Retrofit interfaces on Android.

## 3. Target Direction (`KEEP & EXPAND`)
- **KEEP:** Clean contract sharing between Android and `:backend`.
- **EXPAND:** Add DTOs here for:
  - Completed `TrainingSession` upload payloads.
  - `ScheduledTraining` sync payloads.
  - Community `TrainingPlan` and `Exercise` version comparison DTOs.
  - Global Chat paginated history (`GET /chat/messages`) and structured WebSocket message frames.

## 4. Verification Commands
```bash
./gradlew :dto:test :dto:ktlintCheck
```
