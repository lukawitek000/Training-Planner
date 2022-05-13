package com.lukasz.witkowski.training.planner.training.trainingOverview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.shared.time.formatToString
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import com.lukasz.witkowski.training.planner.ui.components.CategoryChip
import com.lukasz.witkowski.training.planner.ui.components.ExpandableListCardItem
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.LoadingScreen
import com.lukasz.witkowski.training.planner.ui.components.TrainingExerciseRepsSetsTimeOverviewRow
import com.lukasz.witkowski.training.planner.ui.theme.LightDark12
import me.bytebeats.views.charts.line.LineChart
import me.bytebeats.views.charts.line.LineChartData
import me.bytebeats.views.charts.line.render.line.SolidLineDrawer
import me.bytebeats.views.charts.line.render.point.EmptyPointDrawer
import me.bytebeats.views.charts.line.render.xaxis.SimpleXAxisDrawer
import me.bytebeats.views.charts.line.render.yaxis.SimpleYAxisDrawer

@Composable
fun TrainingOverviewScreen(
    modifier: Modifier,
    viewModel: TrainingOverviewViewModel,
    navigateBack: () -> Unit
) {
    val trainingPlan by viewModel.trainingPlan.collectAsState()
    val trainingStatistics by viewModel.trainingStatistics.collectAsState()
    Scaffold(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .padding(8.dp),
        ) {
            item {
                if (trainingPlan is ResultHandler.Loading) {
                    LoadingScreen()
                } else if (trainingPlan is ResultHandler.Success) {
                    TrainingOverviewContent(
                        modifier = Modifier,
                        trainingPlan = (trainingPlan as ResultHandler.Success<TrainingPlan>).value
                    )
                }
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Statistics",
                    fontSize = 26.sp,
                    color = MaterialTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                if (trainingStatistics.isNotEmpty()) {
                    TrainingStatisticsList(
                        modifier = Modifier,
                        trainingStatistics = trainingStatistics
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_statistics),
                            fontSize = 18.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrainingOverviewContent(
    modifier: Modifier = Modifier,
    trainingPlan: TrainingPlan
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = trainingPlan.title,
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (trainingPlan.description.isNotEmpty()) {
            Text(text = trainingPlan.description, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (trainingPlan.exercises.isNotEmpty()) {
            TrainingExercisesExpandableList(
                modifier = Modifier,
                exercises = trainingPlan.exercises
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun TrainingExercisesExpandableList(
    modifier: Modifier,
    exercises: List<TrainingExercise>
) {
    ExpandableListCardItem(
        modifier = modifier,
        shrinkedContent = { Text(text = "Exercises", fontSize = 18.sp) },
        expandedContent = {
            LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
                items(exercises) {
                    SingleTrainingExerciseInformation(
                        modifier = Modifier,
                        exercise = it
                    )
                }
            }
        })
}

@Composable
fun SingleTrainingExerciseInformation(modifier: Modifier, exercise: TrainingExercise) {
    ListCardItem(
        modifier = modifier,
        backgroundColor = LightDark12
    ) {
        Column() {
            Text(
                text = exercise.name,
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = exercise.description, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            CategoryChip(
                modifier = Modifier.fillMaxWidth(),
                category = exercise.category
            )
            if (!exercise.category.isNone()) {
                Spacer(modifier = Modifier.height(16.dp))
            }
            TrainingExerciseRepsSetsTimeOverviewRow(exercise = exercise)
            Spacer(modifier = Modifier.height(16.dp))
            if (exercise.restTime.isNotZero()) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Rest time: ")
                    Text(text = exercise.restTime.toString())
                }
            }
        }
    }
}


@Composable
fun TrainingStatisticsList(
    modifier: Modifier,
    trainingStatistics: List<TrainingStatistics>
) {
    LazyColumn(modifier = modifier.heightIn(max = 500.dp)) {
        items(trainingStatistics) {
            SingleTrainingStatisticsItem(
                modifier = Modifier,
                trainingStatistics = it
            )
        }
    }
}

@Composable
fun SingleTrainingStatisticsItem(
    modifier: Modifier = Modifier,
    trainingStatistics: TrainingStatistics
) {
    val fontSize = 16.sp
    ListCardItem(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = trainingStatistics.date.formatToString(),
                fontSize = fontSize,
                textAlign = TextAlign.End
            )
            Text(
                text = stringResource(
                    id = R.string.time_text,
                    trainingStatistics.totalTime.toString()
                ), fontSize = fontSize
            )
            Text(text = "Effective time: ${trainingStatistics.effectiveTime}", fontSize = fontSize)
        }
    }
}

fun areHeartRateStatisticsAvailable(heartRateDuringTraining: List<Double>) =
    heartRateDuringTraining.size > 1 && heartRateDuringTraining.any { it != heartRateDuringTraining.first() }

@Composable
fun HeartRateLineChart(
    modifier: Modifier = Modifier,
    data: List<Double>
) {
    val lineChartData = LineChartData(
        points = data.mapIndexed { index, heartRate ->
            LineChartData.Point(
                heartRate.toFloat(),
                label = ""
            )
        },
        padBy = 50.0f,
        startAtZero = false
    )
    if (areHeartRateStatisticsAvailable(data)) {
        LineChart(
            modifier = modifier
                .heightIn(max = 200.dp)
                .padding(16.dp),
            lineChartData = lineChartData,
            pointDrawer = EmptyPointDrawer,
            lineDrawer = SolidLineDrawer(color = MaterialTheme.colors.primary),
            yAxisDrawer = SimpleYAxisDrawer(
                labelTextColor = MaterialTheme.colors.primaryVariant,
                drawLabelEvery = 3,
                axisLineColor = MaterialTheme.colors.primaryVariant
            ),
            xAxisDrawer = SimpleXAxisDrawer(
                axisLineColor = MaterialTheme.colors.primaryVariant
            ),
            horizontalOffset = 0f
        )
    }
}

@Preview
@Composable
fun HeartRateLineChartPreview() {
    HeartRateLineChart(data = listOf(13.0, 13.0, 13.0))
}


@Preview
@Composable
fun SingleExercisePrev() {
    SingleTrainingExerciseInformation(
        Modifier,
        TrainingExercise(
            id = TrainingExerciseId(""),
            name = "Super exercise",
            description = "Bes exercise for back, watch for yoafalkd, s foihfd  s;odfnf piewkj i  lkjevdkjsbf ",
            category = Category(),
            sets = 10,
            repetitions = 100,
            time = Time(141000),
            restTime = Time(53988)
        )
    )
}


