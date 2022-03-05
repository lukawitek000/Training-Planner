package com.lukasz.witkowski.shared.models.statistics

import androidx.room.Embedded
import androidx.room.Relation

//class TrainingWithStatistics(
//    @Embedded val training: Training,
//    @Relation(
//        entity = TrainingStatistics::class,
//        parentColumn = "id",
//        entityColumn = "trainingId"
//    )
//    val statistics: List<TrainingCompleteStatistics>
//)