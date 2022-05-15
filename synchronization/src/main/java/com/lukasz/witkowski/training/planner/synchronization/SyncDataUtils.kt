package com.lukasz.witkowski.shared.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

const val TRAINING_PATH = "/training"
const val STATISTICS_PATH = "/statistics"

val gson: Gson = GsonBuilder().create()

const val INTEGER_VALUE_BUFFER_SIZE = 4
