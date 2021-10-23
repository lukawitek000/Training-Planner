package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun CategoryChip(
    modifier: Modifier = Modifier,
    isSelected: Boolean = true,
    text: String,
    selectionChanged: (Boolean) -> Unit = {},
    isClickable: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier.clip(shape = shape).then(if(isClickable) Modifier.clickable { selectionChanged(!isSelected) } else Modifier) ,
        elevation = 4.dp,
        color = if (isSelected) Color.Yellow else Color.DarkGray
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(8.dp),
            fontSize = 16.sp
        )
    }
}


@Preview
@Composable
fun CategoryChipPreview() {
    CategoryChip(isSelected = true, text = "Shoulders", selectionChanged = {  })
}
