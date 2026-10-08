package com.lukasz.witkowski.training.planner.dto.common

object ApiRoutes {
    const val BASE_PATH = "/api/v1"

    object Auth {
        const val BASE = "$BASE_PATH/auth"
        const val REGISTER = "$BASE/register"
        const val LOGIN = "$BASE/login"
        const val REFRESH = "$BASE/refresh"
        const val ME = "$BASE/me"
    }

    object Exercises {
        const val BASE = "$BASE_PATH/exercises"
        const val CATEGORIES = "$BASE/categories"
        fun byId(id: String) = "$BASE/$id"
    }

    object TrainingPlans {
        const val BASE = "$BASE_PATH/training-plans"
        fun byId(id: String) = "$BASE/$id"
    }

    object WebSockets {
        const val CHAT = "/ws/chat"
    }
}
