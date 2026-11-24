package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.shared.R as SharedR
import com.lukasz.witkowski.training.planner.shared.theme.Motion
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    config: TopBarConfig?,
    onAction: (TopBarAction) -> Unit,
    navigateBack: () -> Unit
) {
    if (config == null) return
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            AnimatedVisibility(
                visible = config.hasBackArrow,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = Motion.FADE_THROUGH_IN_DURATION_MS,
                        delayMillis = Motion.FADE_THROUGH_DELAY_MS,
                        easing = LinearOutSlowInEasing
                    )
                ) + expandHorizontally(
                    animationSpec = tween(Motion.ENTER_DURATION_MS, easing = FastOutSlowInEasing)
                ),
                exit = fadeOut(
                    animationSpec = tween(Motion.FADE_THROUGH_OUT_DURATION_MS, easing = FastOutLinearInEasing)
                ) + shrinkHorizontally(
                    animationSpec = tween(Motion.EXIT_DURATION_MS, easing = FastOutLinearInEasing)
                )
            ) {
                IconButton(onClick = { navigateBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back"
                    )
                }
            }
        },
        title = {
            AnimatedContent(
                targetState = config.title,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = Motion.FADE_THROUGH_IN_DURATION_MS,
                            delayMillis = Motion.FADE_THROUGH_DELAY_MS,
                            easing = LinearOutSlowInEasing
                        )
                    ) togetherWith fadeOut(
                        animationSpec = tween(
                            durationMillis = Motion.FADE_THROUGH_OUT_DURATION_MS,
                            easing = FastOutLinearInEasing
                        )
                    )
                },
                label = "TopBarTitleAnimation"
            ) { title ->
                Text(text = title)
            }
        },
        actions = {
            TopBarActionsMenu(
                items = config.menuItems,
                onAction = onAction
            )
        }
    )
}

@Composable
private fun TopBarActionsMenu(
    items: List<TopBarMenuItem>,
    onAction: (TopBarAction) -> Unit,
) {
    val (iconItem, overflowItem) = items.partition { it is TopBarMenuItem.IconItem }
    iconItem.filterIsInstance<TopBarMenuItem.IconItem>().forEach { item ->
        IconButton(onClick = { onAction(item.action) }) {
            Icon(
                imageVector = item.icon,
                contentDescription = null
            )
        }
    }
    if (overflowItem.isNotEmpty()) {
        OverflowMenu(overflowItem, onAction)
    }
}

@Composable
private fun OverflowMenu(
    overflowItem: List<TopBarMenuItem>,
    onAction: (TopBarAction) -> Unit
) {
    var showMenu by rememberSaveable(overflowItem) { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { showMenu = true },
            modifier = Modifier.testTag("OverflowMenuButton")
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            overflowItem.filterIsInstance<TopBarMenuItem.OverflowItem>().forEach { item ->
                val color = item.color ?: MaterialTheme.colorScheme.onSurface
                DropdownMenuItem(
                    text = { Text(item.text, color = color) },
                    leadingIcon = {
                        Icon(imageVector = item.icon, contentDescription = null, tint = color)
                    },
                    modifier = Modifier.testTag("MenuItem_${item.text}"),
                    onClick = {
                        showMenu = false
                        onAction(item.action)
                    }
                )
            }
        }
    }
}

@AppPreview
@Composable
private fun TopBarPreview() {
    TrainingPlannerTheme {
        TopBar(
            navigateBack = {},
            onAction = {},
            config = TopBarConfig(
                "Hello",
                hasBackArrow = true,
                menuItems = listOf(
                    TopBarMenuItem.OverflowItem(
                        icon = Icons.Default.Edit,
                        text = stringResource(R.string.edit),
                        action = TopBarAction.EditExercise(ExerciseId.create())
                    ),
                    TopBarMenuItem.OverflowItem(
                        icon = Icons.Default.Delete,
                        text = stringResource(SharedR.string.delete),
                        action = TopBarAction.DeleteExercise(ExerciseId.create())
                    ),
                )
            )
        )
    }
}
