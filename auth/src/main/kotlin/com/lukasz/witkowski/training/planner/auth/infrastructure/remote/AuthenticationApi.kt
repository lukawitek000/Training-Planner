package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.auth.AuthResponseDto
import com.lukasz.witkowski.training.planner.dto.auth.LoginRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.RefreshTokenRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.RegisterRequestDto
import com.lukasz.witkowski.training.planner.dto.auth.TokenResponseDto
import com.lukasz.witkowski.training.planner.dto.common.ApiRoutes
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthenticationApi {

    @POST(ApiRoutes.Auth.REGISTER)
    suspend fun register(@Body registerRequestDto: RegisterRequestDto): AuthResponseDto

    @POST(ApiRoutes.Auth.LOGIN)
    suspend fun login(@Body loginRequestDto: LoginRequestDto): AuthResponseDto

    @POST(ApiRoutes.Auth.REFRESH)
    suspend fun refresh(@Body refreshTokenRequestDto: RefreshTokenRequestDto): TokenResponseDto
}
