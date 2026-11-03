---
name: compose-ui-workflow
description: >-
  Use when creating, refactoring, or visually verifying Jetpack Compose screens,
  design system components (App* prefix), ViewModels, UDF state/intent flows,
  or @AppPreview functions.
---

# Compose UI & Visual Verification Workflow Skill

Use this skill whenever building or modifying Jetpack Compose UI components, feature screens, or presentation-layer ViewModels.

## Step 1: Enforce Project Composable Guidelines
Read [`docs/composable_guidelines.md`](../../../docs/composable_guidelines.md) before creating new UI components. Ensure every composable adheres to:
1. **Component Reuse & `App` Prefix:** Check `:shared/src/main/kotlin/com/lukasz/witkowski/training/planner/shared/ui/` first (`AppButton`, `AppCard`, `AppTextInputField`, `AppCircularTimer`, `AppFilterChip`, `AppEmptyState`, `AppErrorState`, `AppLoadingOverlay`). Any new reusable component in `:shared` must be prefixed with `App`.
2. **Zero Raw `.dp` or `.sp` Literals:** Never import `androidx.compose.ui.unit.dp` or `.sp` in feature/UI composables. Source all spacing, radii, borders, and icon sizes from [`Dimens.kt`](../../../shared/src/main/kotlin/com/lukasz/witkowski/training/planner/shared/theme/Dimens.kt). If a token is missing, add it to `Dimens.kt` first.
3. **Theme Tokens Only:** Use `MaterialTheme.colorScheme`, `MaterialTheme.typography`, and `MaterialTheme.shapes`.
4. **Unidirectional Data Flow (UDF):**
   - Expose immutable `StateFlow<UiState>` from the ViewModel.
   - Pass state down and events/intents up via lambdas or a single sealed `Intent` handler (`processIntent`).
   - Keep `NavDisplay` / `TrainingPlannerNavigator` calls in `:app`'s `Navigation.kt` rather than coupling screens to navigation controllers.

## Step 2: Mandatory `@AppPreview`
Every composable file must contain at least one `private` preview function:
```kotlin
@AppPreview
@Composable
private fun ExampleComponentPreview() {
    TrainingPlannerTheme {
        // Render realistic states (e.g., populated, empty, conflict/error)
    }
}
```

## Step 3: Visual Verification via `compose-preview` Sidecar
After saving your code edits:
1. Activate the **`compose-preview`** skill (`/Users/lukaszwitkowski/.gemini/config/plugins/android-cli-plugin/skills/compose-preview/SKILL.md`) to render the updated `@AppPreview` function(s) in the Android Compose Preview pane (`gradle://.?:<module>@debug`).
2. Inspect the rendered image and component tree (checking bounds, text truncation, dark theme contrast, and accessibility semantics).
3. Run `./gradlew :<module>:testDebugUnitTest :<module>:ktlintCheck`.
