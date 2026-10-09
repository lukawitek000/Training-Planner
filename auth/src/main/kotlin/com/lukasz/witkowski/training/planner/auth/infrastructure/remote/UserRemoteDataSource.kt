package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.domain.model.User
import com.lukasz.witkowski.training.planner.auth.domain.model.UserFailure
import com.lukasz.witkowski.training.planner.shared.utils.AppResult

interface UserRemoteDataSource {
    suspend fun getUserProfile(accessToken: String): AppResult<User, UserFailure>
}
