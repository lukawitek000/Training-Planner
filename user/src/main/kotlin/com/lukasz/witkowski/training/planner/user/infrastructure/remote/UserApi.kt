package com.lukasz.witkowski.training.planner.user.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.auth.UserDto
import retrofit2.http.GET

interface UserApi {

    @GET("/me")
    suspend fun me(): UserDto
}
