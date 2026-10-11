package com.lukasz.witkowski.training.planner.image

data class ImageSaveFailedException(val imageId: ImageId) :
    RuntimeException("Image save failed for the id $imageId")
