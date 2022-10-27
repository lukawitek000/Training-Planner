package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.lukasz.witkowski.training.planner.ui.theme.LightDark12
import com.lukasz.witkowski.training.planner.ui.theme.LightDark5

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    items: List<NavItem> = NavItem.BottomNavItems.list,
    backStackEntry: NavBackStackEntry?,
    onItemClick: (NavItem) -> Unit
) {
    BottomNavigation(
        modifier = modifier,
        elevation = 5.dp
    ) {
        items.forEach { item ->
            if (item.icon == null) return@forEach
            val selected = item.route == backStackEntry?.destination?.route
            BottomNavigationItem(
                selected = selected,
                onClick = { onItemClick(item) },
                selectedContentColor = MaterialTheme.colors.primary,
                unselectedContentColor = Color.Black,
                icon = {
                    NavigationIcon(item)
                }
            )
        }
    }
}

@Composable
private fun NavigationIcon(item: NavItem) {
    if(item.icon == null) return
    Column(
        Modifier.padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.weight(3f),
            painter = painterResource(id = item.icon),
            contentDescription = item.title
        )
        Text(
            modifier = Modifier.weight(2f),
            text = item.title,
            textAlign = TextAlign.Center
        )
    }
}
