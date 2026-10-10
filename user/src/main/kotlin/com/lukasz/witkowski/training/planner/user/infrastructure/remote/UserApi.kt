package com.lukasz.witkowski.training.planner.user.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.auth.UserDto
import com.lukasz.witkowski.training.planner.dto.common.ApiRoutes
import retrofit2.http.GET

interface UserApi {

    @GET(ApiRoutes.Auth.ME)
    suspend fun me(): UserDto
}
