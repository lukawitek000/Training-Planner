# `:user` Module — User Profile & Sync Preferences

## 1. Current Reality (Scope & Key Packages)
- **Namespace:** `com.lukasz.witkowski.training.planner.user`
- **Domain (`domain/`):** `User`, `UserFailure`, `WeightUnit`, `UserRepository`, `UserPreferencesStorage`.
- **Infrastructure (`infrastructure/`):**
  - `local/AndroidUserPreferencesStorage.kt`: Jetpack DataStore Preferences implementation storing user profile cache, `WeightUnit`, and background sync toggle. Implements both `UserPreferencesStorage` and `:network`'s `SyncPreferencesStorage`.
  - `remote/UserApi.kt` & `RetrofitUserRemoteDataSource.kt`: Calls `/api/v1/auth/me`.
  - `DefaultUserRepository.kt`: Coordinates profile retrieval, sign-out (`tokenStorage.clear()`), and `SyncScheduler`.
- **Presentation (`presentation/`):** `UserProfileScreen.kt`, `UserProfileViewModel.kt`, `UserProfileIcon.kt`, `ReauthBottomSheet.kt`, `SignOutBottomSheet.kt`.

## 2. Architecture & Coding Invariants
- Keep user preferences reactive via `Flow` backed by DataStore Preferences (`AndroidUserPreferencesStorage`).
- Follow `docs/composable_guidelines.md` (`App*` components, `Dimens.*`, `@AppPreview`) for all profile screens and bottom sheets.

## 3. Target Direction (`KEEP & EXPAND`)
- **KEEP:** Profile management, sign-out, and `SyncPreferencesStorage` integration.
- **EXPAND:** Add user preference for **"Automatically upload completed training sessions"** (Product Direction Decision #11) to `UserPreferencesStorage` / `SyncPreferencesStorage` and expose the toggle in `UserProfileScreen`.

## 4. Verification Commands
```bash
./gradlew :user:testDebugUnitTest :user:ktlintCheck
```
