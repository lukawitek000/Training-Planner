package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun ExpandableListCardItem(
    modifier: Modifier = Modifier,
    shrinkedContent: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val angle: Float by animateFloatAsState(
        targetValue = if (!isExpanded) 0f else 180f,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing)
    )
    ListCardItem(modifier = modifier, onCardClicked = { isExpanded = !isExpanded }) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                shrinkedContent()
                Icon(
                    Icons.Default.ArrowDropDown,
                    modifier = Modifier.rotate(angle),
                    contentDescription = stringResource(id = R.string.expand_arrow)
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                expandedContent()
            }
        }
    }
}

@Preview
@Composable
fun ExpandableListCardItemPreview() {
    TrainingPlannerTheme {
        ExpandableListCardItem(
            shrinkedContent = {
                Column {
                    Text("Shrinked content")
                    Text(text = "Column 2nd value")
                }
                              },
            expandedContent = {
                Column() {
                    Text(text = "Expanded content")
                    Text(text = "Expanded content")
                    Text(text = "Expanded content")
                    Text(text = "Expanded content")
                }
            }
        )
    }

}