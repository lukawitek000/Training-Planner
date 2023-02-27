package com.lukasz.witkowski.training.planner.statistics.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbTrainingStatistics
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbTrainingWithExercisesStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date

internal fun TrainingStatistics.toDbTrainingStatistics(): DbTrainingWithExercisesStatistics {
    return DbTrainingWithExercisesStatistics(
        trainingStatistics = DbTrainingStatistics(
            id = id.toString(),
            trainingPlanId = trainingPlanId.toString(),
            totalTime = totalTime.timeInMillis,
            date = date.time
        ),
        exercisesStatistics = exercisesStatistics.map { it.toDbExerciseStatistics(id) }
    )
}

internal fun DbTrainingWithExercisesStatistics.toTrainingStatistics(): TrainingStatistics {
    return TrainingStatistics(
        id = TrainingStatisticsId(trainingStatistics.id),
        trainingPlanId = TrainingPlanId(trainingStatistics.trainingPlanId),
        totalTime = Time(trainingStatistics.totalTime),
        date = Date(trainingStatistics.date),
        exercisesStatistics = exercisesStatistics.map { it.toExerciseStatistics() }
    )
}
