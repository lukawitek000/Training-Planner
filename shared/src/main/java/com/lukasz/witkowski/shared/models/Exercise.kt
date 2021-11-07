package com.lukasz.witkowski.shared.models

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = "",
    val description: String = "",
    val category: Category = Category.None,
    val image: Bitmap? = null
)
