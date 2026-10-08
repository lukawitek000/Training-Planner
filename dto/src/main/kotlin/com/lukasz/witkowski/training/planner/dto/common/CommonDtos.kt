package com.lukasz.witkowski.training.planner.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class PagedResponseDto<T>(
    val items: List<T>,
    val page: Int,
    val limit: Int,
    val totalItems: Long,
    val totalPages: Int
)

@Serializable
data class ApiErrorDto(
    val statusCode: Int,
    val message: String,
    val details: String? = null
)
