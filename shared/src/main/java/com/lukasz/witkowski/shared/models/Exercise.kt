package com.lukasz.witkowski.shared.models

import android.graphics.Bitmap

data class Exercise(
    val id: Long = 0L,
    val name: String,
    val description: String,
    val category: Category,
    val image: Bitmap
)
