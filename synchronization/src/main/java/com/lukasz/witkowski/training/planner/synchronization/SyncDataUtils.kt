package com.lukasz.witkowski.training.planner.synchronization

import com.google.gson.Gson
import com.google.gson.GsonBuilder

const val TRAINING_PATH = "/training"
const val STATISTICS_PATH = "/statistics"

const val ACKNOWLEDGE_FLAG = 1

val gson: Gson = GsonBuilder().create()
