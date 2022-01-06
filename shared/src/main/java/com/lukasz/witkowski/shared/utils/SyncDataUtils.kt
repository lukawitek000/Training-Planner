package com.lukasz.witkowski.shared.utils

import com.google.gson.GsonBuilder
import com.lukasz.witkowski.shared.models.Category

const val TRAINING_PATH = "/training"
const val TRAINING_KEY = "training"

val gson = GsonBuilder()
    .registerTypeAdapter(Category::class.java, CategoryAdapter())
    .create()

