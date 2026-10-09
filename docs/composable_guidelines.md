# Composable Generation Guidelines for Training Planner

This document establishes the architecture, conventions, and guidelines for generating UI composable components in the Training Planner Android project.

---

## 1. Placement & Package Location Protocol

Before creating a new UI component, **always confirm or ask the user** where the composable should be placed if the location is ambiguous.

### Default Subpackages in `:shared` Module
UI components live under `com.lukasz.witkowski.training.planner.shared.ui`:

| UI Component Category | Subpackage Path | Example Components |
|---|---|---|
| **Input Fields & Labels** | `shared/ui/inputField/` | `AppInputLabel`, `AppTextField`, `AppLabeledTextField` |
| **Buttons** | `shared/ui/button/` | `AppButton`, `AppPrimaryButton`, `AppSecondaryButton`, `AppTertiaryButton` |
| **Segmented Controls** | `shared/ui/segmentedControl/` | `AppSegmentedControl`, `AppSegmentedControlItem` |
| **Cards & Dividers** | `shared/ui/card/` | `AppCard`, `AppCardDivider` |
| **Icons & Badges** | `shared/ui/appIcon/` | `AppIconBox` |

> [!TIP]
> If a component does not fit into any existing category, ask the user if a new subpackage should be created under `shared/ui/<category>/` or placed in a feature module.

---

## 2. Naming Conventions

To prevent naming collisions with standard Jetpack Compose Material 3, Foundation, or third-party libraries:

1. **Prefix**: Prefix all custom design system components with `App` (e.g., `AppTextField` instead of `TextField` or `TextInputField`).
2. **Subpackage Organization**: Place composables in categorized subpackages matching their design purpose.
3. **Overloads**: Provide convenience overloads accepting Compose slots (`@Composable (() -> Unit)?`) as well as direct vector icon parameters (`ImageVector?`).

---

## 3. Material 3 & Design Token Integration

Always build UI components using modern Jetpack Compose Material 3 foundation components and design tokens.

### Theme Tokens (`Theme.kt`, `Color.kt`, `Type.kt`, `Shape.kt`)
- **Colors**: Access colors via `MaterialTheme.colorScheme` (e.g. `primaryContainer`, `onPrimary`, `surfaceContainerHigh`, `surfaceBright`, `onSurfaceVariant`, `outlineVariant`).
- **Typography**: Access font styles via `MaterialTheme.typography` (e.g. `labelMedium`, `titleSmall`, `titleMedium`, `bodyLarge`).
- **Shapes**: Access corner radii via `MaterialTheme.shapes` (e.g. `small` for 8dp, `medium` for 12dp, `large` for 16dp, `CircleShape` for full rounded badges).

### Dimensions & Spacing (`Dimens2.kt`)
- **No Raw `dp` Literals**: **Never** import or hardcode raw `.dp` or `.sp` literals in UI composable files.
- **Source from `Dimens2`**: Sourced all spacing, margins, gaps, border widths, elevations, and icon sizes directly from `Dimens2` (e.g. `Dimens2.spaceMd`, `Dimens2.buttonHeight`, `Dimens2.thinBorder`, `Dimens2.shadowSm`, `Dimens2.iconSize16`, `Dimens2.zeroDp`).
- **Missing Tokens**: If a required dimension token is missing, add it to `Dimens2.kt` first before referencing it in UI composables.

---

## 4. Preview Requirements (`@AppPreview`)

Every generated composable file must include a `private` preview function:

1. **Custom Preview Annotation**: Annotate preview functions with `@AppPreview` (`com.lukasz.witkowski.training.planner.shared.ui.AppPreview`). This custom annotation automatically configures `showBackground = true` and `backgroundColor = 0xFF1A120A` (Dark Surface Theme Background), eliminating raw color literal duplication across previews.
2. **Theme Wrapper**: Always wrap preview content inside `TrainingPlannerTheme2 { ... }`.
3. **Realistic States**: Showcase realistic component states (e.g. empty vs. filled, normal vs. premium, enabled vs. disabled).

---

## 5. Code Pattern Blueprint

```kotlin
package com.lukasz.witkowski.training.planner.shared.ui.category

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Component description and design specification details.
 */
@Composable
fun AppExampleComponent(
    text: String,
    modifier: Modifier = Modifier,
) {
    // Implementation using MaterialTheme and Dimens2 tokens
}

@AppPreview
@Composable
private fun AppExampleComponentPreview() {
    TrainingPlannerTheme2 {
        AppExampleComponent(
            text = "Example",
            modifier = Modifier.padding(Dimens2.spaceMd),
        )
    }
}
```
