package com.lukasz.witkowski.training.planner.user.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * TopBar trailing profile icon button composable.
 */
@Composable
fun UserProfileIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "User Profile",
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@AppPreview
@Composable
private fun UserProfileIconPreview() {
    TrainingPlannerTheme2 {
        UserProfileIcon(onClick = {})
    }
}
