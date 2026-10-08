package com.lukasz.witkowski.training.planner.backend.plugins

import com.lukasz.witkowski.training.planner.backend.routing.authRoutes
import com.lukasz.witkowski.training.planner.backend.routing.chatWsRoutes
import com.lukasz.witkowski.training.planner.backend.routing.exerciseRoutes
import com.lukasz.witkowski.training.planner.backend.routing.trainingPlanRoutes
import com.lukasz.witkowski.training.planner.backend.service.AuthService
import com.lukasz.witkowski.training.planner.backend.service.ExerciseService
import com.lukasz.witkowski.training.planner.backend.service.TrainingPlanService
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting(
    authService: AuthService,
    exerciseService: ExerciseService,
    trainingPlanService: TrainingPlanService
) {
    routing {
        authRoutes(authService)
        exerciseRoutes(exerciseService)
        trainingPlanRoutes(trainingPlanService)
        chatWsRoutes()
    }
}
