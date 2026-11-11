package com.lukasz.witkowski.training.planner.session.domain.session

enum class TrainingSetsPolicyType {
    STRAIGHT_SETS
}

object TrainingSetsPolicyFactory {
    fun create(type: TrainingSetsPolicyType): TrainingSetsPolicy = when (type) {
        TrainingSetsPolicyType.STRAIGHT_SETS -> StraightSetsPolicy()
    }
}
