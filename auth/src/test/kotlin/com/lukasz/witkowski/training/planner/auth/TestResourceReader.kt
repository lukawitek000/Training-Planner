package com.lukasz.witkowski.training.planner.auth

object TestResourceReader {
    fun readJson(fileName: String): String {
        return this::class.java.classLoader
            ?.getResourceAsStream("json/$fileName")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("Resource json/$fileName not found")
    }
}
