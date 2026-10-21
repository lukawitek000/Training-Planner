package com.lukasz.witkowski.training.planner.network

interface AuthSessionProvider {
    suspend fun isLoggedIn(): Boolean
}
