package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    items: List<NavItem> = NavItem.BottomNavItems.list,
    backStackEntry: NavBackStackEntry?,
    onItemClick: (NavItem) -> Unit
) {
    NavigationBar(
        modifier = modifier,
    ) {
        items.forEach { item ->
            if (item.icon == null) return@forEach
            val selected = item.route == backStackEntry?.destination?.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Black
                ),
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
