package com.lukasz.witkowski.training.planner.training.trainingSession

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.statistics.domain.ExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.TrainingStatistics

@Composable
fun TrainingSessionSummaryScreen(
    modifier: Modifier = Modifier,
    statistics: TrainingStatistics
) {

    Column(modifier.fillMaxSize()) {
        Text(text = "Training statistics")
        Text(text = "Total time ${statistics.totalTime.toString()}")
        Text(text = "Effective time ${statistics.effectiveTime.toString()}")
        Divider()
        ExercisesStatistics(
            exercisesStatistics = statistics.exercisesStatistics
        )
    }

}

@Composable
fun ExercisesStatistics(
    modifier: Modifier = Modifier,
    exercisesStatistics: List<ExerciseStatistics>
) {
    LazyColumn {
        items(exercisesStatistics) { exerciseStatistics ->
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .border(3.dp, Color.Red, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(text = "Exercise Id ${exerciseStatistics.trainingExerciseId}")
                Text(text = "Total time ${exerciseStatistics.totalTime}")
                Text(text = "Completeness rate ${exerciseStatistics.completenessRate}")
            }
        }
    }
}
