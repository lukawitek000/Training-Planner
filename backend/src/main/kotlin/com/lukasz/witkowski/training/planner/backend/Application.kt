package com.lukasz.witkowski.training.planner.backend

import com.lukasz.witkowski.training.planner.backend.db.DatabaseFactory
import com.lukasz.witkowski.training.planner.backend.plugins.configureRouting
import com.lukasz.witkowski.training.planner.backend.plugins.configureSecurity
import com.lukasz.witkowski.training.planner.backend.plugins.configureSerialization
import com.lukasz.witkowski.training.planner.backend.plugins.configureSockets
import com.lukasz.witkowski.training.planner.backend.plugins.configureStatusPages
import com.lukasz.witkowski.training.planner.backend.service.AuthService
import com.lukasz.witkowski.training.planner.backend.service.ExerciseService
import com.lukasz.witkowski.training.planner.backend.service.TrainingPlanService
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    val authService = AuthService()
    val exerciseService = ExerciseService()
    val trainingPlanService = TrainingPlanService(exerciseService)

    runBlocking {
        exerciseService.ensureDefaultCategoriesExist()
    }

    configureSerialization()
    configureSecurity()
    configureStatusPages()
    configureSockets()
    configureRouting(authService, exerciseService, trainingPlanService)
}
