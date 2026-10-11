package com.lukasz.witkowski.training.planner.exercise.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.common.ApiRoutes
import com.lukasz.witkowski.training.planner.dto.common.PagedResponseDto
import com.lukasz.witkowski.training.planner.dto.exercise.CategoryDto
import com.lukasz.witkowski.training.planner.dto.exercise.CreateExerciseRequestDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.UpdateExerciseRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ExerciseApi {
    @GET(ApiRoutes.Exercises.CATEGORIES)
    suspend fun getCategories(): List<CategoryDto>

    @GET(ApiRoutes.Exercises.BASE)
    suspend fun getExercises(
        @Query("query") query: String? = null,
        @Query("category") categoryId: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): PagedResponseDto<ExerciseDto>

    @GET("${ApiRoutes.Exercises.BASE}/{id}")
    suspend fun getExerciseById(@Path("id") id: String): ExerciseDto

    @POST(ApiRoutes.Exercises.BASE)
    suspend fun createExercise(@Body request: CreateExerciseRequestDto): ExerciseDto

    @PUT("${ApiRoutes.Exercises.BASE}/{id}")
    suspend fun updateExercise(
        @Path("id") id: String,
        @Body request: UpdateExerciseRequestDto
    ): ExerciseDto

    @DELETE("${ApiRoutes.Exercises.BASE}/{id}")
    suspend fun deleteExercise(@Path("id") id: String): Response<Unit>
}
