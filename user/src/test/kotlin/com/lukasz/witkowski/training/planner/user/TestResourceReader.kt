package com.lukasz.witkowski.training.planner.user

object TestResourceReader {
    fun readJson(fileName: String): String = this::class.java.classLoader
        ?.getResourceAsStream("json/$fileName")
        ?.bufferedReader()
        ?.use { it.readText() }
        ?: error("Resource json/$fileName not found")
}
