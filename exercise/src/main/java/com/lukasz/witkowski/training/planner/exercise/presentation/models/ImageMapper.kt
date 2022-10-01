package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.exercise.domain.Image as DomainImage

object ImageMapper {

    fun toImage(domainImage: DomainImage?): Image? {
        return domainImage?.let { Image(domainImage.id, domainImage.path) }
    }

    fun toImage(image: Image?): DomainImage? {
        return image?.let {DomainImage(image.imageId, image.path) }
    }
}