package com.lukasz.witkowski.training.planner.user.infrastructure.remote

import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.user.domain.model.User
import com.lukasz.witkowski.training.planner.user.domain.model.UserFailure

interface UserRemoteDataSource {
    suspend fun getUserProfile(): AppResult<User, UserFailure>
}
