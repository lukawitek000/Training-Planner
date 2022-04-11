package com.lukasz.witkowski.training.planner.statistics.infrastructure

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.TrainingStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.DbTrainingStatistics
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.DbTrainingWithExercisesStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date

object TrainingStatisticsMapper {

    fun toDbTrainingStatistics(trainingStatistics: TrainingStatistics): DbTrainingWithExercisesStatistics {
        return DbTrainingWithExercisesStatistics(
            trainingStatistics = DbTrainingStatistics(
                id = trainingStatistics.id.value,
                trainingPlanId = trainingStatistics.trainingPlanId.value,
                totalTime = trainingStatistics.totalTime.timeInMillis,
                date = trainingStatistics.date.time
            ),
            exercisesStatistics = trainingStatistics.exercisesStatistics.map {
                ExerciseStatisticsMapper.toDbExerciseStatistics(it, trainingStatistics.id)
            }
        )
    }

    fun toTrainingStatistics(dbTrainingWithExercisesStatistics: DbTrainingWithExercisesStatistics): TrainingStatistics {
        val dbTrainingStatistics = dbTrainingWithExercisesStatistics.trainingStatistics
        return TrainingStatistics(
            id = TrainingStatisticsId(dbTrainingStatistics.id),
            trainingPlanId = TrainingPlanId(dbTrainingStatistics.trainingPlanId),
            totalTime = Time(dbTrainingStatistics.totalTime),
            date = Date(dbTrainingStatistics.date),
            exercisesStatistics = dbTrainingWithExercisesStatistics.exercisesStatistics.map {
                ExerciseStatisticsMapper.toExerciseStatistics(it)
            }
        )
    }

}