# `:app` Module — Application Entry Point & Root Navigation

## 1. Current Reality (Scope & Key Files)
- **Namespace:** `com.lukasz.witkowski.training.planner`
- **Entry Points:** `TrainingPlannerApplication` (starts Koin with `appModule`), `MainActivity`.
- **DI Graph (`di/AppModule.kt`):** Includes `networkModule`, `authModule`, `userModule`, `exerciseModule`, `trainingModule`, `sessionModule`, and `analyticsModule`. Provides `BuildConfig.BASE_URL` (`http://10.0.2.2:8080/` on debug) via `NetworkQualifiers.BASE_URL`.
- **Navigation (`navigation/`):** Uses **Jetpack Navigation 3** (`androidx.navigation3.ui.NavDisplay`, `EntryProviderScope`, `DialogSceneStrategy`, `LocalResultEventBus`, `ResultEffect`). All top-level and feature routes (`TrainingPlannerNavKey`) are wired in `Navigation.kt` and `TrainingPlannerNavigator.kt`.
- **Misplaced Feature Screens (`training/editor/`):** Currently hosts `AddTrainingExerciseScreen.kt`, `TrainingExerciseConfigurationScreen.kt`, and `TrainingExerciseConfigurationViewModel.kt`.

## 2. Architecture & Coding Invariants
- Activate the **`navigation-3`** and **`compose-ui-workflow`** skills when modifying navigation backstacks, dialogs, or result passing (`LocalResultEventBus`).
- Keep business logic out of `:app`; `:app` should only coordinate cross-module navigation and root DI assembly.

## 3. Target Direction & Known Tech Debt (`KEEP / REFACTOR / MOVE`)
- **MOVE:** `AddTrainingExerciseScreen` and `TrainingExerciseConfigurationScreen`/`ViewModel` belong in `:training` (or a feature navigation builder).
- **REFACTOR:** `:session-service` and `:image` are not directly wired in `AppModule.kt` (`imageModule` is transitive via `exerciseModule`). Wire `:session-service` when refactoring foreground workout execution.
- **REMOVE:** Unused `libs.androidx.compose.navigation` (Navigation 2) and `libs.google.playServicesWearable` dependencies in `app/build.gradle.kts` once verified.

## 4. Verification Commands
```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```
