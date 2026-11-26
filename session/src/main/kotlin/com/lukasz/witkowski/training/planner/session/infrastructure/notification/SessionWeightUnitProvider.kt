package com.lukasz.witkowski.training.planner.session.infrastructure.notification

import com.lukasz.witkowski.training.planner.shared.utils.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun interface SessionWeightUnitProvider {
    fun weightUnitFlow(): Flow<WeightUnit>

    companion object {
        val Default = SessionWeightUnitProvider { flowOf(WeightUnit.KG) }
    }
}
