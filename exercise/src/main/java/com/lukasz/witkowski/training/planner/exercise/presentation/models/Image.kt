package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.exercise.domain.ImageId

data class Image(
    val imageId: ImageId = ImageId.create(),
    val path: String
)