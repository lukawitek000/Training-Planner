package com.lukasz.witkowski.training.planner.statistics.infrastructure

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.TrainingStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.DbTrainingStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId

object TrainingStatisticsMapper {

    fun toDbTrainingStatistics(trainingStatistics: TrainingStatistics): DbTrainingStatistics {
        return DbTrainingStatistics(
            id = trainingStatistics.id.value,
            trainingPlanId = trainingStatistics.trainingPlanId.value,
            totalTime = trainingStatistics.totalTime.timeInMillis,
            exercisesStatistics = trainingStatistics.exercisesStatistics.map {
                ExerciseStatisticsMapper.toDbExerciseStatistics(
                    it
                )
            }
        )
    }

    fun toTrainingStatistics(dbTrainingStatistics: DbTrainingStatistics): TrainingStatistics {
        return TrainingStatistics(
            id = TrainingStatisticsId(dbTrainingStatistics.id),
            trainingPlanId = TrainingPlanId(dbTrainingStatistics.trainingPlanId),
            totalTime = Time(dbTrainingStatistics.totalTime),
            exercisesStatistics = dbTrainingStatistics.exercisesStatistics.map {
                ExerciseStatisticsMapper.toExerciseStatistics(
                    it
                )
            }
        )
    }

}