# `:shared` Module — Design System, Theme Tokens & Core Utilities

## 1. Current Reality (Scope & Key Packages)
- **Namespace:** `com.lukasz.witkowski.training.planner.shared`
- **Design System (`shared/ui/`):** Reusable Material 3 components prefixed with `App`:
  - `button/` (`AppButton`, `AppPrimaryButton`, `AppSecondaryButton`, `AppTertiaryButton`)
  - `inputField/` (`AppTextInputField`, `AppLabeledTextInputField`, `AppSearchBar`, `AppInputLabel`)
  - `card/` (`AppCard`, `AppCardDivider`), `bento/` (`AppBentoTile`)
  - `chips/` (`AppFilterChip`, `AppCategoryPill`), `badges/` (`AppStatusBadge`, `AppMetadataPill`)
  - `feedback/` (`AppEmptyState`, `AppErrorState`, `AppLoadingOverlay`), `loading/` (`AppLoadingState`)
  - `dialogs/` (`AppConfirmationDialog`), `segmentedControl/` (`AppSegmentedControl`)
  - `timer/` (`AppCircularTimer`), `timepicker/` (`AppTimePickerBottomSheet`), `appIcon/` (`AppIconBox`)
- **Theme & Previews (`shared/theme/`, `shared/ui/AppPreview.kt`):** `TrainingPlannerTheme`, `Dimens`, `Color`, `Type`, `Shape`, and `@AppPreview`.
- **Core Utilities (`shared/utils/`, `shared/time/`, `shared/di/`, `shared/network/`):**
  - `AppResult<T, E>` and `ResultHandler` for functional error handling.
  - `Time`, `TimeProvider`, `SystemTimeProvider`, `TestTimeProvider`, `TimeFormatter`.
  - `CoroutineDispatchersModule` (`CoroutineDispatcherQualifiers.IO`, `DEFAULT`, `MAIN`).
  - `NetworkFailure` enum.

## 2. Architecture & Coding Invariants
- Follow [`docs/composable_guidelines.md`](../docs/composable_guidelines.md) strictly:
  - **No raw `.dp` or `.sp` literals** in UI composables—always reference `Dimens.*` (add new tokens to `Dimens.kt` first).
  - Prefix all shared UI components with `App`.
  - Every UI component file must include a `private` `@AppPreview` function wrapped in `TrainingPlannerTheme`.
- Activate the **`compose-preview`** skill to visually render and verify `@AppPreview` changes.

## 3. Target Direction & Known Tech Debt (`KEEP / REFACTOR`)
- **KEEP:** Core design system, `Dimens`, `AppResult`, and `TimeProvider` abstractions.
- **REFACTOR:** `shared/build.gradle.kts` currently exposes `api(projects.dto)` and `api(libs.retrofit)` / `api(libs.okhttp)` even though `:shared` only defines `NetworkFailure`. Move HTTP-specific dependencies to `:network` so `:shared` remains a pure UI + foundational utility module.

## 4. Verification Commands
```bash
./gradlew :shared:testDebugUnitTest :shared:ktlintCheck
```
