package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "SyncMetadata")
data class DbSyncMetadata(
    @PrimaryKey
    val key: String,
    val value: Long
)
