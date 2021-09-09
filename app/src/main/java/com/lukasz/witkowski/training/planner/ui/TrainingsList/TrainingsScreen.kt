package com.lukasz.witkowski.training.planner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lukasz.witkowski.shared.Training
import com.lukasz.witkowski.shared.dummyTrainingList
import com.lukasz.witkowski.training.planner.ui.TrainingsList.TrainingsListViewModel


@Composable
fun TrainingsScreen(innerPadding: PaddingValues = PaddingValues(), viewModel: TrainingsListViewModel){
    val trainings = remember { viewModel.getAllTrainings() }
    LazyColumn(
        contentPadding = innerPadding
    ) {
        items(trainings) { training ->  TrainingListItemContent(training = training)}
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

    Card(
        modifier = modifier
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.DarkGray
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = training.name,
                    fontSize = 24.sp
                )
                Text(
                    text = training.description,
                    fontSize = 14.sp
                )
            }
            Icon(
                modifier = Modifier.size(40.dp),
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Start training",
                tint = Color.Yellow,
            )
        }
    }

}


@Preview(showBackground = true)
@Composable
fun TrainingsScreenPreview() {
    TrainingsScreen(viewModel = hiltViewModel<TrainingsListViewModel>())
}