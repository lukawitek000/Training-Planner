package com.lukasz.witkowski.training.planner

import androidx.compose.material.SnackbarResult
import kotlinx.coroutines.CoroutineScope

data class SnackbarState(
    val scope: CoroutineScope,
    private val show: suspend (message: String, actionLabel: String?) -> SnackbarResult
) {

    suspend fun show(message: String, actionLabel: String? = null): SnackbarResult {
        return show.invoke(message, actionLabel)
    }

}
