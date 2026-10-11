package com.lukasz.witkowski.training.planner.image

data class ImageNotFoundException(val imageId: ImageId) :
    RuntimeException("Image not found for the id $imageId")
