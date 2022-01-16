package com.lukasz.witkowski.training.planner.ui.trainingOverview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.shared.models.Category
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.models.statistics.GeneralStatistics
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.ui.components.CategoryChip
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.theme.LightDark12
import me.bytebeats.views.charts.line.LineChart
import me.bytebeats.views.charts.line.LineChartData
import me.bytebeats.views.charts.line.render.line.SolidLineDrawer
import me.bytebeats.views.charts.line.render.point.EmptyPointDrawer
import me.bytebeats.views.charts.line.render.xaxis.SimpleXAxisDrawer
import me.bytebeats.views.charts.line.render.yaxis.SimpleYAxisDrawer
import timber.log.Timber

@ExperimentalAnimationApi
@Composable
fun TrainingOverviewScreen(
    modifier: Modifier,
    viewModel: TrainingOverviewViewModel,
    navigateBack: () -> Unit
) {
    val trainingWithExercises by viewModel.training.collectAsState(
        TrainingWithExercises(
            Training(
                title = ""
            ), emptyList()
        )
    )
    val generalStatistics by viewModel.statistics.collectAsState(emptyList())
    Scaffold(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .padding(8.dp),
        ) {
            item {
                TrainingOverviewContent(
                    modifier = Modifier,
                    trainingWithExercises = trainingWithExercises
                )
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
                if (generalStatistics.isNotEmpty()) {
                    TrainingStatisticsList(
                        modifier = Modifier,
                        generalStatistics = generalStatistics
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

@ExperimentalAnimationApi
@Composable
fun TrainingOverviewContent(
    modifier: Modifier = Modifier,
    trainingWithExercises: TrainingWithExercises
) {
    val training = trainingWithExercises.training
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = training.title,
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (training.description.isNotEmpty()) {
            Text(text = training.description, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (trainingWithExercises.exercises.isNotEmpty()) {
            TrainingExercisesExpandableList(
                modifier = Modifier,
                exercises = trainingWithExercises.exercises
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@ExperimentalAnimationApi
@Composable
fun TrainingExercisesExpandableList(modifier: Modifier, exercises: List<TrainingExercise>) {

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
                Text(text = "Exercises", fontSize = 18.sp)
                Icon(
                    Icons.Default.ArrowDropDown,
                    modifier = Modifier.rotate(angle),
                    contentDescription = "Expand exercises list arrow"
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
                    items(exercises) {
                        SingleTrainingExerciseInformation(
                            modifier = Modifier,
                            exercise = it
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SingleTrainingExerciseInformation(modifier: Modifier, exercise: TrainingExercise) {
    ListCardItem(
        modifier = modifier,
        backgroundColor = LightDark12
    ) {
        Column() {
            Text(
                text = exercise.exercise.name,
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = exercise.exercise.description, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            if (exercise.exercise.category != Category.None) {
                CategoryChip(
                    modifier = Modifier.fillMaxWidth(),
                    text = exercise.exercise.category.name
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Reps:")
                    Text(text = exercise.repetitions.toString())
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Sets:")
                    Text(text = exercise.sets.toString())
                }
                if (exercise.time > 0L) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Time:")
                        Text(text = TimeFormatter.millisToTime(exercise.time))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (exercise.restTime > 0L) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Rest time: ")
                    Text(text = TimeFormatter.millisToTime(exercise.restTime))
                }
            }
        }
    }
}

@Composable
fun TrainingStatisticsList(
    modifier: Modifier,
    generalStatistics: List<GeneralStatistics>
) {
    LazyColumn(modifier = modifier.heightIn(max = 500.dp)) {
        items(generalStatistics) {
            SingleTrainingStatisticsItem(
                modifier = Modifier,
                generalStatistics = it
            )
        }
    }
}

@Composable
fun SingleTrainingStatisticsItem(
    modifier: Modifier = Modifier,
    generalStatistics: GeneralStatistics
) {
    val time = TimeFormatter.millisToTime(generalStatistics.time)
    val burnedCalories =
        if (generalStatistics.burnedCalories == 0.0) stringResource(R.string.no_data) else stringResource(
            id = R.string.total_burned_calories, generalStatistics.burnedCalories
        )
    val maxHeartRateStatistics =
        if (generalStatistics.maxHeartRate == 0.0) stringResource(R.string.no_data) else stringResource(
            id = R.string.max_heart_rate, generalStatistics.maxHeartRate
        )
    val fontSize = 16.sp
    ListCardItem(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = TimeFormatter.convertMillisToDate(generalStatistics.date),
                fontSize = fontSize,
                textAlign = TextAlign.End
            )
            Text(text = stringResource(id = R.string.time_text, time), fontSize = fontSize)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.burned_calories_text, burnedCalories),
                fontSize = fontSize
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.max_heart_rate_text, maxHeartRateStatistics),
                fontSize = fontSize
            )
            if(areHeartRateStatisticsAvailable(generalStatistics.heartRateDuringTraining)) {
                Spacer(modifier = Modifier.height(8.dp))
                ListCardItem(
                    backgroundColor = LightDark12
                ) {
                    Column() {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.heart_rate_plot),
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HeartRateLineChart(
                            modifier = Modifier,
                            data = generalStatistics.heartRateDuringTraining
                        )

                    }
                }
            }
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
        points = data.mapIndexed { index, heartRate -> LineChartData.Point(heartRate.toFloat(), label = "") },
        padBy = 50.0f,
        startAtZero = false
    )
    if(areHeartRateStatisticsAvailable(data)) {
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
fun SingleTrainingStatisticsItemPrev() {
    SingleTrainingStatisticsItem(
        generalStatistics = GeneralStatistics(
            0L, 60000L, System.currentTimeMillis(), 12.1, 123.0, heartRateDuringTraining = listOf()
        )
    )
}


@Preview
@Composable
fun SingleExercisePrev() {
    SingleTrainingExerciseInformation(
        Modifier,
        TrainingExercise(
            exercise = Exercise(
                name = "Super exercise",
                description = "Bes exercise for back, watch for yoafalkd, s foihfd  s;odfnf piewkj i  lkjevdkjsbf ",
                category = Category.Back
            ),
            sets = 10,
            repetitions = 100,
            time = 141000,
            restTime = 53988
        )
    )
}


