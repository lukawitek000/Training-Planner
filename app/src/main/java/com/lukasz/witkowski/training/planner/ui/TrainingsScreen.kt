package com.lukasz.witkowski.training.planner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.shared.Training
import com.lukasz.witkowski.shared.dummyTrainingList


@Composable
fun TrainingsScreen(innerPadding: PaddingValues = PaddingValues()){
    val trainings = remember { dummyTrainingList }
    LazyColumn(
        contentPadding = innerPadding
    ) {
        items(
            count = trainings.size,
            itemContent = { index ->
                TrainingListItemContent(training = trainings[index])
            }
        )
    }
}

//
//@Composable
//fun ListCardItem(
//    modifier: Modifier = Modifier,
//    content: @Composable (Training) -> Unit
//) {
//    Box(
//        modifier = modifier
//            .background(Color.LightGray)
//    ){
//        content()
//    }
//}

@Composable
fun TrainingListItemContent(
    modifier: Modifier = Modifier,
    training: Training
) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray)
            .padding(8.dp),
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
                    .padding(8.dp),
                imageVector = Icons.Filled.Person,
                contentDescription = "${training.name} image"
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = training.name,
                    fontSize = 24.sp
                )
                Text(
                    text = training.description,
                    fontSize = 14.sp
                )
            }
        }
    }

}


@Preview(showBackground = true)
@Composable
fun TrainingsScreenPreview() {
    TrainingsScreen()
}