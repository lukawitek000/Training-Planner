package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

class BottomBarItem(
    val icon: Int,
    val title: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)

@Composable
fun BottomNavigationBar(
    items: List<BottomBarItem>,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                modifier = Modifier.testTag("BottomNavItem_$index"),
                selected = item.selected,
                onClick = { item.onClick() },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent
                ),
                icon = {
                    NavigationIcon(item)
                },
                label = {
                    Text(
                        text = item.title,
                        textAlign = TextAlign.Center
                    )
                }
            )
        }
    }
}

@Composable
private fun NavigationIcon(item: BottomBarItem) {
    Icon(
        painter = painterResource(id = item.icon),
        contentDescription = item.title,
        modifier = Modifier.size(Dimens.iconSize24)
    )
}

@AppPreview
@Composable
private fun BottomNavigationBarPreview() {
    TrainingPlannerTheme {
        BottomNavigationBar(
            modifier = Modifier.fillMaxWidth(),
            items = listOf(
                BottomBarItem(
                    icon = R.drawable.trainings_icon,
                    title = "Training Plans",
                    selected = true,
                    onClick = { }
                ),
                BottomBarItem(
                    icon = R.drawable.exercises_icon,
                    title = "Exercises",
                    selected = false,
                    onClick = { }
                ),
            ),
        )
    }
}