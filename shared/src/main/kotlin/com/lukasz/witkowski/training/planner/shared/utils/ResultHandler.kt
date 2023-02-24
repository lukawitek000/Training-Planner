package com.lukasz.witkowski.training.planner.shared.utils

sealed class ResultHandler<out T : Any> {
    data class Success<out T : Any>(val value: T) : ResultHandler<T>()
    data class Error(val message: String, val cause: Exception? = null) : ResultHandler<Nothing>()
    object Loading : ResultHandler<Nothing>()
    object Idle : ResultHandler<Nothing>()
}
