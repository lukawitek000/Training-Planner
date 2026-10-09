package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.auth.UserDto
import retrofit2.http.GET
import retrofit2.http.Header

interface UserApi {

    @GET("/me")
    suspend fun me(@Header("Authorization") bearerToken: String): UserDto
}
